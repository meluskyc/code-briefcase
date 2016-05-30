'use strict';

describe('Service: Tag', function () {

  // load the service's module
  beforeEach(module('codebriefcaseApp'));

  // instantiate service
  var tag;
  beforeEach(inject(function (_tag_) {
    tag = _tag_;
  }));

  it('should do something', function () {
    expect(!!tag).toBe(true);
  });

});
