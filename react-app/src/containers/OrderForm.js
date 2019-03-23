import React from 'react';
import PropTypes from 'prop-types';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux';
import { Formik } from "formik";
import * as Yup from "yup";
import Paper from "@material-ui/core/Paper";
import { withStyles } from '@material-ui/core/styles';
import { saveOrder, selectSaveError, selectSaving } from '../redux/order';
import MuiOrderForm from './MuiOrderForm';

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
  }
});

const validationSchema = Yup.object({
  threadId: Yup.string("Enter a thread Id"),
  customerId: Yup.string("Enter your customer Id")
    .required("Enter your customer Id"),
  state: Yup.string("Enter order status")
    .required("Confirm order status")
    .oneOf(["ACCEPTED", "CREATED", "COOKING", "ONROUTE", "PENDING", "DELIVERED"], "Invalid status")
});

function OrderForm(props) {
  const { classes, saveOrder, fetchError, saving } = props;
  
  const initialValues = {
    threadId: "",
    customerId: "5c960f41684e1567ef42fb69",
    state: "DONE",
    created: new Date(),
    modified: new Date()
  };

  return (
    <main className={classes.layout} >
      <Paper className={classes.paper}>
        <Formik
          initialValues={initialValues}
          isInitialValid={true}
          validationSchema={validationSchema}
          onSubmit={(values, { setSubmitting }) => {
            setTimeout(() => {
              saveOrder(values);
              //alert(JSON.stringify(values, null, 2));
              setSubmitting(false);
            }, 1);
          }}
          render={formikProps =>
            <MuiOrderForm {...formikProps}
              fetchError={fetchError}
              saving={saving}/>
          }
        />
      </Paper>
    </main >
  );
}

OrderForm.propTypes = {
  classes: PropTypes.object.isRequired,
};

const mapDispatchToProps = dispatch =>
  bindActionCreators({ saveOrder }, dispatch);

const mapStateToProps = state => ({
  fetchError: selectSaveError(state),
  saving: selectSaving(state)
})

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withStyles(styles)(OrderForm))