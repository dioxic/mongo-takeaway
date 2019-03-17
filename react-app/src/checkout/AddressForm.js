import React from 'react';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import TextField from '@material-ui/core/TextField';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Checkbox from '@material-ui/core/Checkbox';

function AddressForm({
	values,
	errors,
	touched,
	handleChange,
	handleBlur
}) {
	return (
		<React.Fragment>
			<Typography variant="h6" gutterBottom>
				Shipping address
			</Typography>
			<Grid container spacing={24}>
				<Grid item xs={12} sm={6}>
					<TextField
						required
						id="firstName"
						name="firstName"
						label="First name"
						fullWidth
						autoComplete="fname"
						helperText={touched.firstName ? errors.firstName : ""}
						error={touched.firstName && Boolean(errors.firstName)}
						onChange={handleChange}
						onBlur={handleBlur}
						value={values.firstName}
					/>
				</Grid>
				<Grid item xs={12} sm={6}>
					<TextField
						required
						id="lastName"
						name="lastName"
						label="Last name"
						fullWidth
						autoComplete="lname"
						helperText={touched.lastName ? errors.lastName : ""}
						error={touched.lastName && Boolean(errors.lastName)}
						onChange={handleChange}
						onBlur={handleBlur}
						value={values.lastName}
					/>
				</Grid>
				<Grid item xs={12}>
					<TextField
						required
						id="address1"
						name="address1"
						label="Address line 1"
						fullWidth
						autoComplete="billing address-line1"
						helperText={touched.address1 ? errors.address1 : ""}
						error={touched.address1 && Boolean(errors.address1)}
						onChange={handleChange}
						onBlur={handleBlur}
						value={values.address1}
					/>
				</Grid>
				<Grid item xs={12}>
					<TextField
						id="address2"
						name="address2"
						label="Address line 2"
						fullWidth
						autoComplete="billing address-line2"
						helperText={touched.address2 ? errors.address2 : ""}
						error={touched.address2 && Boolean(errors.address2)}
						onChange={handleChange}
						onBlur={handleBlur}
						value={values.address2}
					/>
				</Grid>
				<Grid item xs={12} sm={6}>
					<TextField
						required
						id="city"
						name="city"
						label="City"
						fullWidth
						autoComplete="billing address-level2"
						helperText={touched.city ? errors.city : ""}
						error={touched.city && Boolean(errors.city)}
						onChange={handleChange}
						onBlur={handleBlur}
						value={values.city}
					/>
				</Grid>
				<Grid item xs={12} sm={6}>
					<TextField id="state"
						name="state"
						label="State/Province/Region"
						fullWidth
						helperText={touched.state ? errors.state : ""}
						error={touched.state && Boolean(errors.state)}
						onChange={handleChange}
						onBlur={handleBlur}
						value={values.state}
					/>
				</Grid>
				<Grid item xs={12} sm={6}>
					<TextField
						required
						id="zip"
						name="zip"
						label="Zip / Postal code"
						fullWidth
						autoComplete="billing postal-code"
						helperText={touched.zip ? errors.zip : ""}
						error={touched.zip && Boolean(errors.zip)}
						onChange={handleChange}
						onBlur={handleBlur}
						value={values.zip}
					/>
				</Grid>
				<Grid item xs={12} sm={6}>
					<TextField
						required
						id="country"
						name="country"
						label="Country"
						fullWidth
						autoComplete="billing country"
						helperText={touched.country ? errors.country : ""}
						error={touched.country && Boolean(errors.country)}
						onChange={handleChange}
						onBlur={handleBlur}
						value={values.country}					
					/>
				</Grid>
				<Grid item xs={12}>
					<FormControlLabel
						control={<Checkbox color="secondary" name="saveAddress" value="yes" />}
						label="Use this address for payment details"
					/>
				</Grid>
			</Grid>
		</React.Fragment>
	);
}

export default AddressForm;