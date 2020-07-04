'use strict';

var fs = require('fs');

var $ = require('jquery'),
    BpmnModeler = require('bpmn-js/lib/Modeler');

var propertiesPanelModule = require('bpmn-js-properties-panel'),
    propertiesProviderModule = require('bpmn-js-properties-panel/lib/provider/camunda'),
    camundaModdleDescriptor = require('camunda-bpmn-moddle/resources/camunda');

var apiEndpoint = '/* @echo apiEndpoint */';

var container = $('#js-drop-zone');

var canvas = $('#js-canvas');

var bpmnModeler = new BpmnModeler({
  container: canvas,
  propertiesPanel: {
    parent: '#js-properties-panel'
  },
  additionalModules: [
    propertiesPanelModule,
    propertiesProviderModule
  ],
  moddleExtensions: {
    camunda: camundaModdleDescriptor
  }
});

var newDiagramXML = fs.readFileSync(__dirname + '/../resources/newDiagram.bpmn', 'utf-8');

function createNewDiagram() {
  openDiagram(newDiagramXML);
}

function openDiagramById(deploymentId) {
	  $.ajax({
	      url: apiEndpoint + "/engine-rest/engine/default/deployment/" + deploymentId + "/xml"
	  }).then(function(data) {
		  openDiagram(data.xml);
	  });
	}

function openDiagram(xml) {

  bpmnModeler.importXML(xml, function(err) {

    if (err) {
      container
        .removeClass('with-diagram')
        .addClass('with-error');

      container.find('.error pre').text(err.message);

      console.error(err);
    } else {
      container
        .removeClass('with-error')
        .addClass('with-diagram');
    }


  });
}

function saveSVG(done) {
  bpmnModeler.saveSVG(done);
}

function saveDiagram(done) {

  bpmnModeler.saveXML({ format: true }, function(err, xml) {
    done(err, xml);
  });
}

function saveDiagramToServer(done) {
	 bpmnModeler.saveXML({ format: true }, function(err, xml) {
		    done(err, xml);
		  });
}

function saveBpmnToServer() {
	$('#info').html( "Saving ..." );
	bpmnModeler.saveXML({ format: true }, function(err, xml) {
		$.ajax({
			url: apiEndpoint + '/engine-rest/engine/default/deployment/create',
		    dataType: 'json',
		    type: 'post',
		    contentType: 'multipart/form-data',
		    headers: {
		    	'name':'some nice name',
		        'deployment-name':'test5.bpmn',
		        'Content-Type':'multipart/form-data'
		    },
		    data: xml,
		    processData: false,
		    success: function( data, textStatus, jQxhr ){
		        $('#info').html( "Saved successfully!" );
		    },
		    error: function( jqXhr, textStatus, errorThrown ){
		    	$('#info').html( "Saving failed! Check you bpmn." + errorThrown);
		        console.log( errorThrown );
		    }
		});
	});
}

function registerFileDrop(container, callback) {

  function handleFileSelect(e) {
    e.stopPropagation();
    e.preventDefault();

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
  registerFileDrop(container, openDiagram);
}

// bootstrap diagram functions

$(document).on('ready', function() {

  $('#js-create-diagram').click(function(e) {
    e.stopPropagation();
    e.preventDefault();

    createNewDiagram();
  });
  
  $('#js-create-diagram').click(function(e) {
	  
  });
  
  $.ajax({
      url: apiEndpoint + "/engine-rest/engine/default/deployment"
  }).then(function(data) {
	  var items = [];
	  $.each( data, function( num ) {
	    items.push( "<a id='myLink" + num + "' href='#'>" + (num + 1) + ". " + data[num].source + "   " + data[num].deploymentTime + "</a><br>" );
	  });
	  $( "<div/>", {
		    "class": "my-new-list",
		    html: items.join( "" )
		  }).appendTo( ".diagrams" );
	  $.each( data, function( num ) {
		    $('#myLink' + num).click(function(){ openDiagramById(data[num].id); return false; });
		  });
  });

  var downloadLink = $('#js-download-diagram');
  var downloadSvgLink = $('#js-download-svg');
  var saveBpmnLink = $('#js-save-bpmn');
  saveBpmnLink.click(function(){ saveBpmnToServer(); return false; });

  $('.buttons a').click(function(e) {
    if (!$(this).is('.active')) {
      e.preventDefault();
      e.stopPropagation();
    }
  });

  function setEncoded(link, name, data) {
    var encodedData = encodeURIComponent(data);

    if (data) {
      link.addClass('active').attr({
        'href': 'data:application/bpmn20-xml;charset=UTF-8,' + encodedData,
        'download': name
      });
    } else {
      link.removeClass('active');
    }
  }
  
  function setEncoded2(link, name, data) {
	    var encodedData = encodeURIComponent(data);

	    if (data) {
	    	link.addClass('active');
	    } else {
	      link.removeClass('active');
	    }
	  }

  var debounce = require('lodash/function/debounce');

  var exportArtifacts = debounce(function() {

    saveSVG(function(err, svg) {
      setEncoded(downloadSvgLink, 'diagram.svg', err ? null : svg);
    });

    saveDiagram(function(err, xml) {
      setEncoded(downloadLink, 'diagram.bpmn', err ? null : xml);
    });
    
    saveDiagramToServer(function(err, xml) {
      setEncoded2(saveBpmnLink, 'diagram.bpmn', err ? null : xml);
    });
  }, 500);

  bpmnModeler.on('commandStack.changed', exportArtifacts);
});
