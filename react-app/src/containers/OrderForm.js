import React, { Component } from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import { Formik } from "formik";
import Paper from "@material-ui/core/Paper";
import { withStyles } from '@material-ui/core/styles';
import Card from '@material-ui/core/Card';
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import { connect } from 'react-redux';
import { saveOrder } from '../actions';
import Typography from '@material-ui/core/Typography';
import DateFnsUtils from '@date-io/date-fns';
import * as Yup from "yup";
import { MuiPickersUtilsProvider, TimePicker, DatePicker } from 'material-ui-pickers';

const styles = theme => ({
  paper: {
    marginTop: theme.spacing.unit * 8,
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    width: 'auto',
    padding: `${theme.spacing.unit * 5}px ${theme.spacing.unit * 5}px ${theme
      .spacing.unit * 5}px`
  },
  container: {
    maxWidth: "400px"
  }
});

const validationSchema = Yup.object({
  id: Yup.string("Enter a name")
  .required("Id is required"),
  threadId: Yup.string("Enter your email")
    .required("threadId is required"),
  customerId: Yup.number("Enter your customer Id")
    .required("Enter your customer Id"),
  state: Yup.string("Enter order status")
    .required("Confirm order status")
    .oneOf(["DONE", "PENDING"], "Invalid state (must be DONE or PENDING)")
});

// const styles = theme => ({
//   card: {
//     minWidth: 275,
//   },
//   textField: {
//     marginLeft: theme.spacing.unit,
//     marginRight: theme.spacing.unit,
//     width: 200,
//   },
  // container: {
  //   display: 'flex',
  //   flexWrap: 'wrap',
  // },
  // dense: {
  //   marginTop: 19,
  // },
  // menu: {
  //   width: 200,
  // },
// });

class OrderForm extends Component {
  constructor(props) {
    super(props);
    this.state = {};
  }

  render() {

    const { classes } = this.props;
    const values = { id: "123", threadId: "", customerId: "", state: "", created: "", modified: ""};

    return (
      <div className={classes.container}>
        <Paper elevation={1} className={classes.paper}>
          <Typography className={classes.title} color="textSecondary" gutterBottom>
            New Order
          </Typography>
          <Formik
            initialValues={values}
            validationSchema={validationSchema}
            onSubmit={(values, { setSubmitting }) => {
              setTimeout(() => {
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
              classes
            }) => (
              <form onSubmit={handleSubmit}>
                  <TextField
                    id="id"
                    name="id"
                    label="Order Id"
                    helperText={touched.id ? errors.id : ""}
                    error={touched.id && Boolean(errors.id)}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    value={values.id}
                    margin='normal'
                  />
                  <TextField
                    id="threadId"
                    name="threadId"
                    label="Thread Id"
                    helperText={touched.threadId ? errors.threadId : ""}
                    error={touched.threadId && Boolean(errors.threadId)}              
                    onChange={handleChange}
                    onBlur={handleBlur}
                    value={values.threadId}
                    margin='normal'
                  />
                  <TextField
                    id="customerId"
                    name="customerId"
                    label="Customer Id"
                    helperText={touched.customerId ? errors.customerId : ""}
                    error={touched.customerId && Boolean(errors.customerId)}              
                    onChange={handleChange}
                    onBlur={handleBlur}
                    value={values.customerId}
                    margin='normal'
                  />
                  <TextField
                    id="state"
                    name="state"
                    label="Status"
                    helperText={touched.state ? errors.state : ""}
                    error={touched.state && Boolean(errors.state)}              
                    onChange={handleChange}
                    onBlur={handleBlur}
                    value={values.state}
                    margin='normal'
                  />
                  {/* <MuiPickersUtilsProvider utils={DateFnsUtils}><DatePicker
                    id="created"
                    label="Created Date"
                    helperText={touched.name ? errors.name : ""}
                    error={touched.name && Boolean(errors.name)}              
                    format={this.props.getFormatString({
                      dateFns: "dd/MM/yyyy",
                    })}
                    mask={value =>
                      // handle clearing outside if value can be changed outside of the component
                      value ? [/\d/, /\d/, "/", /\d/, /\d/, "/", /\d/, /\d/, /\d/, /\d/] : []
                    }                  
                    value={created}
                    onChange={change.bind(null, "created")}
                    disableOpenOnEnter
                    animateYearScrolling={false}
                  />
                  <DatePicker
                    id="modified"
                    label="Modified Date"
                    helperText={touched.name ? errors.name : ""}
                    error={touched.name && Boolean(errors.name)}              
                    format={this.props.getFormatString({
                      dateFns: "dd/MM/yyyy",
                    })}
                    mask={value =>
                      // handle clearing outside if value can be changed outside of the component
                      value ? [/\d/, /\d/, "/", /\d/, /\d/, "/", /\d/, /\d/, /\d/, /\d/] : []
                    }                  
                    value={modified}
                    onChange={change.bind(null, "modified")}
                    disableOpenOnEnter
                    animateYearScrolling={false}
                  /></MuiPickersUtilsProvider> */}
                <Button variant="contained" color="primary" type="submit" disabled={isSubmitting}>
                  Add Order
                </Button>
              </form>
          )}
          </Formik>
        </Paper>
      </div>
    );
  }
}

OrderForm.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default connect()(withStyles(styles)(OrderForm))