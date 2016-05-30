'use strict';

angular.module('codebriefcaseApp')
  .factory('Item', function ($resource) {
    return $resource('/api/items/:_id/', {}, {
      'tagsDistinct': {method:'GET', url: '/api/items/tags', isArray:true},
      'update': {method:'PUT'}
    });
  });
