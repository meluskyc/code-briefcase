/**
 * Using Rails-like standard naming convention for endpoints.
 * GET     /api/items              ->  index
 */

'use strict';

// Gets a list of Items
export function index(req, res) {
  res.json([]);
}
