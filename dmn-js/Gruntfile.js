'use strict';

module.exports = function(grunt) {

  require('load-grunt-tasks')(grunt);

  var path = require('path');

  /**
   * Resolve external project resource as file path
   */
  function resolvePath(project, file) {
    return path.join(path.dirname(require.resolve(project)), file);
  }

  // project configuration
  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),
    
    preprocess : {
        options : {
        	context : {
        		apiEndpoint : 'http://localhost:6001/java/v2016/06'
        	}
        },
        dev: {
        	options : {
        		context : {
        			apiEndpoint : 'http://localhost:6001/java/v2016/06'
        		}
        	},
        	src: './dist/index.js',
            dest: './dist/index.js'
        },
        unctad : {
        	options : {
        		context : {
        			apiEndpoint : 'http://unctad.redfunction.ee/java/v2016/06'
        		}
        	},
        	src: './dist/index.js',
            dest: './dist/index.js'
        }
    },

    config: {
      sources: 'app',
      dist: 'dist'
    },

    jshint: {
      src: [
        ['<%=config.sources %>']
      ],
      options: {
        jshintrc: true
      }
    },

    browserify: {
      options: {
        browserifyOptions: {
          // make sure we do not include browser shims unnecessarily
          builtins: false,
          insertGlobalVars: {
            process: function () {
                return 'undefined';
            },
            Buffer: function () {
                return 'undefined';
            }
          }
        }
      },
      watch: {
        options: {
          watch: true
        },
        files: {
          '<%= config.dist %>/index.js': [ '<%= config.sources %>/**/*.js' ]
        }
      },
      app: {
        files: {
          '<%= config.dist %>/index.js': [ '<%= config.sources %>/**/*.js' ]
        }
      }
    },
    less: {
      options: {
        paths: [
          // in order to be able to import "bootstrap/less/**"
          'node_modules'
        ]
      },

      styles: {
        files: { 'dist/css/dmn-js.css': 'node_modules/dmn-js/styles/dmn-js.less' }
      }
    },
    copy: {
      fonts: {
        files: [
          {
            cwd: 'node_modules/dmn-js/fonts/',
            src: 'dmn-js*',
            expand: true,
            dest: '<%= config.dist %>/fonts/'
          }
        ]
      },
      app: {
        files: [
          {
            expand: true,
            cwd: '<%= config.sources %>/',
            src: ['**/*.*', '!**/*.js'],
            dest: '<%= config.dist %>'
          }
        ]
      }
    },
    watch: {
      samples: {
        files: [ '<%= config.sources %>/**/*.*' ],
        tasks: [ 'copy:app' ]
      },
      less: {
        files: ['node_modules/dmn-js/styles/**/*.less'],
        tasks: ['less:styles']
      },
      livereload: {
        options: {
          livereload: 9014
        },
        files: ['<%= config.dist %>/**'],
        tasks: []
      }
    },
    connect: {
      options: {
        port: 9013,
        livereload: 9014,
        hostname: 'localhost'
      },
      livereload: {
        options: {
          open: true,
          base: [
            '<%= config.dist %>'
          ]
        }
      }
    }
  });

  // tasks

  grunt.registerTask('build', [ 'copy', 'less', 'browserify:app', 'preprocess:dev' ]);
  
  grunt.registerTask('unctad', [ 'copy', 'less', 'browserify:app', 'preprocess:unctad' ]);

  grunt.registerTask('auto-build', [
    'copy',
    'less',
    'browserify:watch',
    'preprocess:dev',
    'connect:livereload',
    'watch'
  ]);

  grunt.registerTask('default', [ 'jshint', 'build' ]);
};