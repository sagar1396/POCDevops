'use strict';

module.exports = function(grunt) {

  require('load-grunt-tasks')(grunt);

  var path = require('path');
  
  grunt.loadNpmTasks('grunt-preprocess');

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
          debug: true,
          list: true,
          // make sure we do not include browser shims unnecessarily
          insertGlobalVars: {
            process: function () {
              return 'undefined';
            },
            Buffer: function () {
              return 'undefined';
            }
          }
        },
        transform: [ 'brfs' ]
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

    copy: {
      diagram_js: {
        files: [
          {
            src: resolvePath('diagram-js', 'assets/diagram-js.css'),
            dest: '<%= config.dist %>/css/diagram-js.css'
          }
        ]
      },
      bpmn_js: {
        files: [
          {
            expand: true,
            cwd: resolvePath('bpmn-js', 'assets'),
            src: ['**/*.*', '!**/*.js'],
            dest: '<%= config.dist %>/vendor'
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

    less: {
      options: {
        dumpLineNumbers: 'comments',
        paths: [
          'node_modules'
        ]
      },

      styles: {
        files: {
          'dist/css/app.css': 'styles/app.less'
        }
      }
    },

    watch: {
      options: {
        livereload: true
      },

      samples: {
        files: [ '<%= config.sources %>/**/*.*' ],
        tasks: [ 'copy:app' ]
      },

      less: {
        files: [
          'styles/**/*.less',
          'node_modules/bpmn-js-properties-panel/styles/**/*.less'
        ],
        tasks: [
          'less'
        ]
      },
    },

    connect: {
      livereload: {
        options: {
          port: 9013,
          livereload: true,
          hostname: 'localhost',
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

  grunt.registerTask('default', [ 'jshint', 'env:dev', 'build' ]);
};
