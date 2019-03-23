import React from 'react';
import { Form } from "formik";
import { withStyles } from '@material-ui/core/styles';
import green from '@material-ui/core/colors/green';
import Grid from '@material-ui/core/Grid';
import CircularProgress from '@material-ui/core/CircularProgress';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import MenuItem from '@material-ui/core/MenuItem';
import Typography from '@material-ui/core/Typography';
import { MuiPickersUtilsProvider, DatePicker } from 'material-ui-pickers';
import DateFnsUtils from '@date-io/date-fns';

const styles = theme => ({
	wrapper: {
	  margin: theme.spacing.unit,
	  position: 'relative',
	},
	errorText: {
	  display: 'flex',
	  justifyContent: 'center',
	  alignItems: 'flex-end'
	},
	buttons: {
	  display: 'flex',
	  justifyContent: 'flex-end'
	},
	button: {
	  marginTop: theme.spacing.unit * 5,
	  marginLeft: theme.spacing.unit,
	},
	buttonProgress: {
	  color: green[500],
	  position: 'absolute',
	  top: '50%',
	  left: '50%',
	  marginTop: 8,
	  marginLeft: -10,
	}
});

const orderStates = [
	{
	  value: 'DELIVERED',
	  label: 'Delivered',
	},
	{
	  value: 'ACCEPTED',
	  label: 'Accepted',
	},
	{
	  value: 'CREATED',
	  label: 'Created',
	},
	{
	  value: 'ONROUTE',
	  label: 'On Route',
	},
	{
	  value: 'PENDING',
	  label: 'Pending',
	},
	{
	  value: 'COOKING',
	  label: 'Cooking',
	}
  ];

const MuiOrderForm = ({
	values,
	errors,
	touched,
	handleChange,
	handleBlur,
	isSubmitting,
	setFieldValue,
	isValid,
	classes,
	fetchError,
	saving
  }) => {
	return (<Form>
	  <Typography variant="h6" gutterBottom>
			New Order
	  </Typography>
	  <Grid container spacing={32}>
		<Grid item xs={6} sm={6}>
		  <TextField
			id="id"
			name="id"
			label="Order Id"
			helperText={touched.id ? errors.id : ""}
			error={touched.id && Boolean(errors.id)}
			onChange={handleChange}
			onBlur={handleBlur}
			value={values.id}
			fullWidth
		  />
		</Grid>
		<Grid item xs={6} sm={6}>
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
		<Grid item xs={12} sm={6}>
		  <TextField
			id="customerId"
			name="customerId"
			label="Customer Id"
			helperText={touched.customerId ? errors.customerId : ""}
			error={touched.customerId && Boolean(errors.customerId)}
			onChange={handleChange}
			onBlur={handleBlur}
			value={values.customerId}
			fullWidth
			required
		  />
		</Grid>
		<Grid item xs={12} sm={6}>
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
	  <Grid container spacing={8}>
		<Grid item xs={12} sm={6} className={classes.errorText}>
		  {fetchError && Object.entries(fetchError).length !== 0 &&
			<React.Fragment>
			  <Typography variant="body2" color='error'>
				{fetchError.url} {fetchError.httpStatus} ({fetchError.msg})
			  </Typography>
			</React.Fragment>
		  }
		</Grid>
		<Grid item xs={12} sm={6} className={classes.buttons}>
		  <div className={classes.wrapper}>
			<Button
			  variant="contained"
			  color="primary"
			  type="submit"
			  className={classes.button}
			  disabled={isSubmitting || saving || !isValid}
			>
			  Add Order
			</Button>
			{(isSubmitting || saving) &&
			  <CircularProgress size={24} className={classes.buttonProgress} />
			}
		  </div>
		</Grid>
	  </Grid>
	</Form>
	)
  }

  export default withStyles(styles)(MuiOrderForm)