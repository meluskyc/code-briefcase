'use strict';

describe('Service: Fab', function () {

  // load the service's module
  beforeEach(module('codebriefcaseApp'));

  // instantiate service
  var fab;
  beforeEach(inject(function (_fab_) {
    fab = _fab_;
  }));

  it('should do something', function () {
    expect(!!fab).toBe(true);
  });

});
