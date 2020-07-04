'use strict';

var $ = require('jquery'),
    DmnModeler = require('dmn-js/lib/Modeler');

var apiEndpoint = '/* @echo apiEndpoint */';

var dirty = false;
var originalXML = '';
var latestXML = '';

var container = $('#js-drop-zone');

var downloadLink = $('#js-download-table');
var saveDmnLink = $('#js-save-dmn');
saveDmnLink.click(function(){ saveDmnToServer(); return false; });

var canvas = $('#js-table');

var renderer = new DmnModeler({
  container: canvas,
  keyboard: { bindTo: document },
  minColWidth: 200,
  tableName: 'DMN Table'
});

var newTableXML = require('../resources/newTable.dmn');
var exampleXML = require('../resources/example.dmn');

function createNewTable() {
  openTable(newTableXML);
}
function createDemoTable() {
  openTable(exampleXML);
}

downloadLink.on('click', function() {
  originalXML = latestXML;
  dirty = false;
});

function setEncoded(link, name, data) {
  var encodedData = encodeURIComponent(data);

  dirty = data !== originalXML;
  latestXML = data;

  if (data) {
    link.addClass('active').attr({
      'href': 'data:application/xml;charset=UTF-8,' + encodedData,
      'download': name
    });
  } else {
    link.removeClass('active');
  }
}

function openTableById(key) {
	  $.ajax({
	      url: apiEndpoint + "/engine-rest/engine/default/decision-definition/key/" + key + "/xml"
	  }).then(function(xml) {
		  openTable((new XMLSerializer()).serializeToString(xml));
	  });
	}

function openTable(xml) {

  renderer.importXML(xml, function(err) {

    if (err) {
      container
        .removeClass('with-table')
        .addClass('with-error');

      container.find('.error pre').text(err.message);

      console.error(err);
    } else {
      container
        .removeClass('with-error')
        .addClass('with-table');

      saveTable(function(err, xml) {
        originalXML = xml;
        setEncoded(downloadLink, 'table.dmn', err ? null : xml);
      });
    }


  });
}

function saveTable(done) {

  renderer.saveXML({ format: true }, function(err, xml) {
    done(err, xml);
  });
}

function saveTableToServer(done) {
	renderer.saveXML({ format: true }, function(err, xml) {
		    done(err, xml);
		  });
}

function saveDmnToServer() {
	$('#info').html( "Saving ..." );
	renderer.saveXML({ format: true }, function(err, xml) {
		$.ajax({
		    url: apiEndpoint + '/engine-rest/engine/default/deployment/create',
		    dataType: 'json',
		    type: 'post',
		    contentType: 'multipart/form-data',
		    headers: {
		        'deployment-name':'test5.dmn',
		        'enable-duplicate-filtering':'true',
		        'deploy-changed-only-name': 'mytest',
		        'Content-Type':'multipart/form-data'
		    },
		    data: xml,
		    processData: false,
		    success: function( data, textStatus, jQxhr ){
		        $('#info').html( "Saved successfully!" );
		    },
		    error: function( jqXhr, textStatus, errorThrown ){
		    	$('#info').html( "Saving failed! Check you dmn." + errorThrown);
		        console.log( errorThrown );
		    }
		});
	});
}

function registerFileDrop(container, callback) {

  function handleFileSelect(e) {

    e.stopPropagation();
    e.preventDefault();

    if(dirty && !window.confirm('You made changes to the previous table, ' +
          'do you really want to load the new table and overwrite the changes?')) {
      return;
    }

    var files = e.dataTransfer.files;

    var file = files[0];

    var reader = new FileReader();

    reader.onload = function(e) {

      var xml = e.target.result;

      callback(xml);
    };

    reader.readAsText(file);
  }

  function handleDragOver(e) {
    e.stopPropagation();
    e.preventDefault();

    e.dataTransfer.dropEffect = 'copy'; // Explicitly show this is a copy.
  }

  container.get(0).addEventListener('dragover', handleDragOver, false);
  container.get(0).addEventListener('drop', handleFileSelect, false);
}


////// file drag / drop ///////////////////////

// check file api availability
if (!window.FileList || !window.FileReader) {
  window.alert(
    'Looks like you use an older browser that does not support drag and drop. ' +
    'Try using Chrome, Firefox or the Internet Explorer > 10.');
} else {
  registerFileDrop(container, openTable);
}

// bootstrap table functions

$(document).on('ready', function() {
	
	  $.ajax({
	      url: apiEndpoint + "/engine-rest/engine/default/decision-definition"
	  }).then(function(data) {
		  var items = [];
		  $.each( data, function( num ) {
		    items.push( "<a id='myLink" + num + "' href='#'>" + (num + 1) + ". " + data[num].name + "</a><br>" );
		  });
		  $( "<div/>", {
			    "class": "my-new-list",
			    html: items.join( "" )
			  }).appendTo( ".tables" );
		  $.each( data, function( num ) {
			    $('#myLink' + num).click(function(){ openTableById(data[num].key); return false; });
			  });
	  });

  $('#js-create-table').click(function(e) {
    e.stopPropagation();
    e.preventDefault();

    createNewTable();
    if(window.history && typeof window.history.pushState === 'function') {
      window.history.pushState({},'', window.location.href + '?new');
    }
  });

  $('.use-demo').click(function(e) {
    e.stopPropagation();
    e.preventDefault();

    createDemoTable();
    if(window.history && typeof window.history.pushState === 'function') {
      window.history.pushState({},'', window.location.href + '?example');
    }
  });

  $('.buttons a').click(function(e) {
    if (!$(this).is('.active')) {
      e.preventDefault();
      e.stopPropagation();
    }
  });

  function checkDirty() {
    if (dirty) {
      return 'The changes you performed on the table will be lost upon navigation.';
    }
  }

  var href = window.location.href;
  if(href.indexOf('?new') !== -1) {
    createNewTable();
  } else if(href.indexOf('?example') !== -1) {
    createDemoTable();
  }

  window.onbeforeunload = checkDirty;

  var exportArtifacts = function() {
    saveTable(function(err, xml) {
      setEncoded(downloadLink, 'table.dmn', err ? null : xml);
    });
  };

  renderer.on('commandStack.changed', exportArtifacts);
});