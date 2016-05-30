'use strict';

angular.module('codebriefcaseApp')
  .factory('Fab', function () {
    var Fab = {
      visible: false,
      action: function() {}
    };
    return Fab;
  });
