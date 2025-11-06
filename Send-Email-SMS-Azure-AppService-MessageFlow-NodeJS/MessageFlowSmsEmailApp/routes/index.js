const express = require('express');
const router = express.Router();

// Health message
const healthMessage = 'MessageFlow SMS/Email API app is running. Use POST /sms or POST /email endpoints.';

/* GET home page */
router.get('/', function (req, res, next) {
  res.send(healthMessage);
});

/* POST home page */
router.post('/', function (req, res, next) {
  res.send(healthMessage);
});

module.exports = router;
