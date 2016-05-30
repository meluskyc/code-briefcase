'use strict';

describe('Directive: fab', function () {

  // load the directive's module and view
  beforeEach(module('codebriefcaseApp'));
  beforeEach(module('app/shared/fab/fab.html'));

  var element, scope;

  beforeEach(inject(function ($rootScope) {
    scope = $rootScope.$new();
  }));

  it('should make hidden element visible', inject(function ($compile) {
    element = angular.element('<fab></fab>');
    element = $compile(element)(scope);
    scope.$apply();
    expect(element.text()).toBe('this is the fab directive');
  }));
});
