'use strict';

var app = require('../../../server');
import request from 'supertest';

describe('Item API:', function() {

  describe('GET /api/items', function() {
    var items;

    beforeEach(function(done) {
      request(app)
        .get('/api/items')
        .expect(200)
        .expect('Content-Type', /json/)
        .end((err, res) => {
          if (err) {
            return done(err);
          }
          items = res.body;
          done();
        });
    });

    it('should respond with JSON array', function() {
      items.should.be.instanceOf(Array);
    });

  });

});
