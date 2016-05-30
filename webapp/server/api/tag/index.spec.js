'use strict';

var proxyquire = require('proxyquire').noPreserveCache();

var tagCtrlStub = {
  index: 'tagCtrl.index'
};

var routerStub = {
  get: sinon.spy()
};

// require the index with our stubbed out modules
var tagIndex = proxyquire('./index.js', {
  'express': {
    Router: function() {
      return routerStub;
    }
  },
  './tag.controller': tagCtrlStub
});

describe('Tag API Router:', function() {

  it('should return an express app instance', function() {
    tagIndex.should.equal(routerStub);
  });

  describe('GET /api/tags', function() {

    it('should route to tag.controller.index', function() {
      routerStub.get
        .withArgs('/', 'tagCtrl.index')
        .should.have.been.calledOnce;
    });

  });

});
