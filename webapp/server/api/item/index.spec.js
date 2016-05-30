'use strict';

var proxyquire = require('proxyquire').noPreserveCache();

var itemCtrlStub = {
  index: 'itemCtrl.index'
};

var routerStub = {
  get: sinon.spy()
};

// require the index with our stubbed out modules
var itemIndex = proxyquire('./index.js', {
  'express': {
    Router: function() {
      return routerStub;
    }
  },
  './item.controller': itemCtrlStub
});

describe('Item API Router:', function() {

  it('should return an express app instance', function() {
    itemIndex.should.equal(routerStub);
  });

  describe('GET /api/items', function() {

    it('should route to item.controller.index', function() {
      routerStub.get
        .withArgs('/', 'itemCtrl.index')
        .should.have.been.calledOnce;
    });

  });

});
