'use strict';

var rest = require('restler');

module.exports = function(router) {
  var Action = router.formio.Action;
  /**
   * DmnAction class.
   *   This class is used to create dmn interface.
   *
   * @constructor
   */
  var DmnAction = function(data, req, res) {
    Action.call(this, data, req, res);
  };

  // Derive from Action.
  DmnAction.prototype = Object.create(Action.prototype);
  DmnAction.prototype.constructor = DmnAction;
  DmnAction.info = function(req, res, next) {
    next(null, {
      name: 'dmn',
      title: 'DMN',
      description: 'Allows you to trigger an external DMN interface.',
      priority: 0,
      defaults: {
        handler: ['after'],
        method: ['create', 'update', 'delete']
      }
    });
  };
  DmnAction.settingsForm = function(req, res, next) {
    next(null, [
      {
        label: 'DMN URL',
        key: 'url',
        inputType: 'text',
        defaultValue: '',
        input: true,
        placeholder: 'Call the following URL.',
        prefix: '',
        suffix: '',
        type: 'textfield',
        multiple: false,
        validate: {
          required: true
        }
      },
      {
        label: 'Authorize User',
        key: 'username',
        inputType: 'text',
        defaultValue: '',
        input: true,
        placeholder: 'User for Basic Authentication',
        type: 'textfield',
        multiple: false
      },
      {
        label: 'Authorize Password',
        key: 'password',
        inputType: 'password',
        defaultValue: '',
        input: true,
        placeholder: 'Password for Basic Authentication',
        type: 'textfield',
        multiple: false
      }
    ]);
  };

  /**
   * Trigger the dmns.
   *
   * @param req
   *   The Express request object.
   * @param res
   *   The Express response object.
   * @param cb
   *   The callback function to execute upon completion.
   */
  DmnAction.prototype.resolve = function(handler, method, req, res, next) {
    var options = {};
    if (this.settings.username) {
      options.username = this.settings.username;
      options.password = this.settings.password;
    }
    if (req.method.toLowerCase() === 'delete') {
      options.query = req.params;
      rest.del(this.settings.url, options);
    }
    else {
      // Setup the post data.
      var postData = {
        request: req.body,
        response: res.resource,
        params: req.params
      };

      // Make the request.
      switch (req.method.toLowerCase()) {
        case 'post':
          rest.postJson(this.settings.url, postData, options);
          break;
        case 'put':
          rest.putJson(this.settings.url, postData, options);
          break;
      }
    }

    next();
  };

  // Return the DmnAction.
  return DmnAction;
};
