import React, { useState } from 'react';
import PropTypes from 'prop-types';
import withStyles from '@material-ui/core/styles/withStyles';
import CssBaseline from '@material-ui/core/CssBaseline';
import Paper from '@material-ui/core/Paper';
import Stepper from '@material-ui/core/Stepper';
import Step from '@material-ui/core/Step';
import StepLabel from '@material-ui/core/StepLabel';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import { Formik } from 'formik';
import * as Yup from "yup";
import AddressForm from './AddressForm';
import PaymentForm from './PaymentForm';
import Review from './Review';

const styles = theme => ({
  layout: {
    width: 'auto',
    marginLeft: theme.spacing.unit * 2,
    marginRight: theme.spacing.unit * 2,
    [theme.breakpoints.up(600 + theme.spacing.unit * 2 * 2)]: {
      width: 600,
      marginLeft: 'auto',
      marginRight: 'auto',
    },
  },
  paper: {
    marginTop: theme.spacing.unit * 3,
    marginBottom: theme.spacing.unit * 3,
    padding: theme.spacing.unit * 2,
    [theme.breakpoints.up(600 + theme.spacing.unit * 3 * 2)]: {
      marginTop: theme.spacing.unit * 6,
      marginBottom: theme.spacing.unit * 6,
      padding: theme.spacing.unit * 3,
    },
  },
  stepper: {
    padding: `${theme.spacing.unit * 3}px 0 ${theme.spacing.unit * 5}px`,
  },
  buttons: {
    display: 'flex',
    justifyContent: 'flex-end',
  },
  button: {
    marginTop: theme.spacing.unit * 3,
    marginLeft: theme.spacing.unit,
  },
});

const validationSchema = Yup.object({
  firstName: Yup.string("Enter a first name")
    .required("First name is required"),
  lastName: Yup.string("Enter a last name")
    .required("Last name is required"),
  address1: Yup.string("Enter an address")
    .required("Address is required"),
  city: Yup.string("Enter a last  name")
    .required("City is required"),
  state: Yup.string("Enter a state")
    .required("State is required"),
  zip: Yup.string("Enter a zip")
    .required("Zip is required"),
  country: Yup.string("Enter a country")
    .required("Country is required"),
  cardName: Yup.string("Enter a card name")
    .required("Card name is required"),
  cardNumber: Yup.number("Enter a card number")
    .required("Card number is required"),
  ccv: Yup.number("Enter a CCV")
    .required("CCV is required"),
  expDate: Yup.date("Enter an expiry date")
    .required("Expiry date is required"),
});

const steps = ['Shipping address', 'Payment details', 'Review your order'];

const fieldsByPage = [
  ["firstName", "lastName", "address1", "address2", "city", "state", "zip", "country"],
  ["cardName", "cardNumber", "ccv", "expDate"]
]

function getStepContent(step, props) {
  switch (step) {
    case 0:
      return <AddressForm {...props} />;
    case 1:
      return <PaymentForm {...props} />;
    case 2:
      return <Review {...props} />;
    default:
      throw new Error('Unknown step');
  }
}

const isPageValid = ({ dirty, errors, isInitialValid }, activeStep) => {
  return dirty
    ? errors && Object.keys(errors).filter(field => {
      return fieldsByPage[activeStep].includes(field);
    }).length === 0
    : isInitialValid;
}

function Checkout({ classes, }) {
  const [activeStep, setActiveStep] = useState(0);
  const initialValues = {
    firstName: "Bob",
    lastName: "Biscuit",
    address1: "",
    address2: "",
    city: "",
    state: "",
    zip: "",
    country: "",
    cardName: "",
    cardNumber: "",
    expDate: "",
    ccv: ""
  };

  function handleNext() {
    setActiveStep(activeStep + 1);
  };

  function handleBack() {
    setActiveStep(activeStep - 1);
  };

  function handleReset() {
    setActiveStep(0);
  };

  return (
    <React.Fragment>
      <CssBaseline />
      <main className={classes.layout}>
        <Paper className={classes.paper}>
          <Typography component="h1" variant="h4" align="center">
            Checkout
            </Typography>
          <Stepper activeStep={activeStep} className={classes.stepper}>
            {steps.map(label => (
              <Step key={label}>
                <StepLabel>{label}</StepLabel>
              </Step>
            ))}
          </Stepper>
          <React.Fragment>
            {activeStep === steps.length ? (
              <React.Fragment>
                <Typography variant="h5" gutterBottom>
                  Thank you for your order.
                  </Typography>
                <Typography variant="subtitle1">
                  Your order number is #2001539. We have emailed your order confirmation, and will
                  send you an update when your order has shipped.
                  </Typography>
              </React.Fragment>
            ) : (
                <Formik
                  initialValues={initialValues}
                  validationSchema={validationSchema}
                  onSubmit={(values, { setSubmitting }) => {
                    setTimeout(() => {
                      alert(JSON.stringify(values, null, 2));
                      setSubmitting(false);
                      handleNext();
                    }, 400);
                  }}
                >
                  {(props) => (
                    <form>
                      {getStepContent(activeStep, props)}
                      <div className={classes.buttons}>
                        {activeStep !== 0 && (
                          <Button onClick={handleBack} className={classes.button}>
                            Back
                          </Button>
                        )}
                        <Button
                          variant="contained"
                          color="primary"
                          onClick={activeStep === steps.length - 1 ? props.handleSubmit : handleNext}
                          className={classes.button}
                          disabled={props.isSubmitting || !isPageValid(props, activeStep)}
                        >
                          {activeStep === steps.length - 1 ? 'Place order' : 'Next'}
                        </Button>
                      </div>
                    </form>
                  )}
                </Formik>
              )}
          </React.Fragment>
        </Paper>
      </main>
    </React.Fragment>
  );
}

Checkout.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(Checkout);