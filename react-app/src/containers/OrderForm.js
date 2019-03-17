import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux';
import { saveOrder } from '../redux/order';
import { Formik } from "formik";
import * as Yup from "yup";
import Paper from "@material-ui/core/Paper";
import { withStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import MenuItem from '@material-ui/core/MenuItem';
import Typography from '@material-ui/core/Typography';
import { MuiPickersUtilsProvider, DatePicker } from 'material-ui-pickers';
import DateFnsUtils from '@date-io/date-fns';

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
  buttons: {
    display: 'flex',
    justifyContent: 'flex-end',
  },  
  button: {
    marginTop: theme.spacing.unit * 5,
    marginLeft: theme.spacing.unit,
  },
});

const validationSchema = Yup.object({
  id: Yup.string("Enter an Id")
    .required("Id is required"),
  threadId: Yup.string("Enter a thread Id"),
  customerId: Yup.number("Enter your customer Id")
    .required("Enter your customer Id"),
  state: Yup.string("Enter order status")
    .required("Confirm order status")
    .oneOf(["DONE", "PENDING", "DELIVERED"], "Invalid state (must be DONE or PENDING)")
});

const orderStates = [
  {
    value: 'DONE',
    label: 'Done',
  },
  {
    value: 'DELIVERED',
    label: 'Delivered',
  },
  {
    value: 'PENDING',
    label: 'Pending',
  }
];

class OrderForm extends Component {
  constructor(props) {
    super(props);
    this.state = {};
  }

  render() {
    const { classes, saveOrder } = this.props;
    const values = {
      id: 123,
      threadId: "",
      customerId: "",
      state: "",
      created: new Date(),
      modified: new Date()
    };

    return (
      <main className={classes.layout}>
        <Paper className={classes.paper}>
          <Formik
            initialValues={values}
            validationSchema={validationSchema}
            onSubmit={(values, { setSubmitting }) => {
              setTimeout(() => {
                saveOrder(values);
                alert(JSON.stringify(values, null, 2));
                setSubmitting(false);
              }, 400);
            }}
          >
            {({
              values,
              errors,
              touched,
              handleSubmit,
              handleChange,
              handleBlur,
              isSubmitting,
              setFieldValue,
              isValid
            }) => (
                <form onSubmit={handleSubmit}>
                  <Typography variant="h6" gutterBottom>
                    New Order
                </Typography>
                  <Grid container spacing={32}>
                    <Grid item xs={6} sm={3}>
                      <TextField
                        id="id"
                        name="id"
                        label="Order Id"
                        type="number"
                        helperText={touched.id ? errors.id : ""}
                        error={touched.id && Boolean(errors.id)}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        value={values.id}
                        fullWidth
                        required
                      />
                    </Grid>
                    <Grid item xs={6} sm={3}>
                      <TextField
                        id="threadId"
                        name="threadId"
                        label="Thread Id"
                        type="number"
                        helperText={touched.threadId ? errors.threadId : ""}
                        error={touched.threadId && Boolean(errors.threadId)}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        value={values.threadId}
                        fullWidth
                      />
                    </Grid>
                    <Grid item xs={12} sm={3}>
                      <TextField
                        id="customerId"
                        name="customerId"
                        label="Customer Id"
                        type="number"
                        helperText={touched.customerId ? errors.customerId : ""}
                        error={touched.customerId && Boolean(errors.customerId)}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        value={values.customerId}
                        fullWidth
                        required
                      />
                    </Grid>
                    <Grid item xs={12} sm={3}>
                      <TextField
                        id="state"
                        name="state"
                        label="Status"
                        helperText={touched.state ? errors.state : ""}
                        error={touched.state && Boolean(errors.state)}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        value={values.state}
                        SelectProps={{
                          MenuProps: {
                            className: classes.menu,
                          },
                        }}
                        fullWidth
                        required
                        select
                      >
                        {orderStates.map(option => (
                          <MenuItem key={option.value} value={option.value}>
                            {option.label}
                          </MenuItem>
                        ))}
                      </TextField>
                    </Grid>
                    <MuiPickersUtilsProvider utils={DateFnsUtils}>
                      <Grid item xs={12} sm={6}>
                        <DatePicker
                          id="created"
                          label="Created Date"
                          name="created"
                          helperText={touched.name ? errors.name : ""}
                          error={touched.name && Boolean(errors.name)}
                          format="dd/MM/yyyy"
                          value={values.created}
                          onChange={value => setFieldValue("created", value)}
                          disableOpenOnEnter
                          fullWidth
                          animateYearScrolling={false}
                        />
                      </Grid>
                      <Grid item xs={12} sm={6}>
                        <DatePicker
                          id="modified"
                          label="Modified Date"
                          name="modified"
                          helperText={touched.name ? errors.name : ""}
                          error={touched.name && Boolean(errors.name)}
                          format="dd/MM/yyyy"
                          value={values.modified}
                          onChange={value => setFieldValue("modified", value)}
                          disableOpenOnEnter
                          fullWidth
                          animateYearScrolling={false}
                        />
                      </Grid>
                    </MuiPickersUtilsProvider>
                  </Grid>
                  <div className={classes.buttons}>
                    <Button
                      variant="contained"
                      color="primary"
                      type="submit"
                      className={classes.button}
                      disabled={isSubmitting || !isValid}
                    >
                      Add Order
                    </Button>
                  </div>
                </form>
              )}
          </Formik>
        </Paper>
      </main >
    );
  }
}

OrderForm.propTypes = {
  classes: PropTypes.object.isRequired,
};

const mapDispatchToProps = dispatch =>
  bindActionCreators({ saveOrder }, dispatch);

export default connect(
  null,
  mapDispatchToProps
)(withStyles(styles)(OrderForm))