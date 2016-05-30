'use strict';

describe('Controller: FabCtrl', function () {

  // load the controller's module
  beforeEach(module('codebriefcaseApp'));

  var FabCtrl, scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    FabCtrl = $controller('FabCtrl', {
      $scope: scope
    });
  }));

  it('should ...', function () {
    expect(1).toEqual(1);
  });
});
