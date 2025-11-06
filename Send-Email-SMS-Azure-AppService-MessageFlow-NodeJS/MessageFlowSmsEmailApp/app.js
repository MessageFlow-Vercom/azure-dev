const createError = require('http-errors');
const express = require('express');
const path = require('path');
const cookieParser = require('cookie-parser');
const logger = require('morgan');
const fs = require('fs');

const indexRouter = require('./routes/index');
const smsRouter = require('./routes/sms');
const emailRouter = require('./routes/email');

const { SmsService } = require('./services/smsService');
const { EmailService } = require('./services/emailService');

const app = express();

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'ejs');

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

// Configure logging
const consoleLogger = console;
app.locals.logger = consoleLogger;

// Load configuration
function loadConfig() {
  // First, try to load from environment variables (Azure App Service)
  if (process.env.REST_API_AUTHORIZATION && process.env.REST_API_APPLICATION_KEY) {
    consoleLogger.info('Loading configuration from environment variables');
    return {
      rest_api: {
        authorization: process.env.REST_API_AUTHORIZATION,
        application_key: process.env.REST_API_APPLICATION_KEY
      }
    };
  }

  // Fallback to config.json for local development
  try {
    const configPath = path.join(__dirname, 'config.json');
    const configData = fs.readFileSync(configPath, 'utf8');
    consoleLogger.info('Loading configuration from config.json');
    return JSON.parse(configData);
  } catch (error) {
    if (error.code === 'ENOENT') {
      consoleLogger.error('config.json not found and environment variables not set');
    } else if (error instanceof SyntaxError) {
      consoleLogger.error(`Error parsing config.json: ${error.message}`);
    } else {
      consoleLogger.error(`Error loading config: ${error.message}`);
    }
    return {};
  }
}

const config = loadConfig();

// Initialize services
let smsService = null;
let emailService = null;

try {
  smsService = new SmsService(config);
  emailService = new EmailService(config);
  app.locals.smsService = smsService;
  app.locals.emailService = emailService;
  consoleLogger.info('Services initialized successfully');
} catch (error) {
  consoleLogger.error(`Failed to initialize services: ${error.message}`);
}

// Mount routes
app.use('/', indexRouter);
app.use('/sms', smsRouter);
app.use('/email', emailRouter);

// catch 404 and forward to error handler
app.use(function (req, res, next) {
  next(createError(404));
});

// error handler
app.use(function (err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});

module.exports = app;
