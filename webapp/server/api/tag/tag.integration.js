'use strict';

var app = require('../../../server');
import request from 'supertest';

describe('Tag API:', function() {

  describe('GET /api/tags', function() {
    var tags;

    beforeEach(function(done) {
      request(app)
        .get('/api/tags')
        .expect(200)
        .expect('Content-Type', /json/)
        .end((err, res) => {
          if (err) {
            return done(err);
          }
          tags = res.body;
          done();
        });
    });

    it('should respond with JSON array', function() {
      tags.should.be.instanceOf(Array);
    });

  });

});
