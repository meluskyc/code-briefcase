'use strict';

describe('Service: Sidebar', function () {

  // load the service's module
  beforeEach(module('codebriefcaseApp'));

  // instantiate service
  var listFilter;
  beforeEach(inject(function (_listFilter_) {
    listFilter = _listFilter_;
  }));

  it('should do something', function () {
    expect(!!listFilter).toBe(true);
  });

});
