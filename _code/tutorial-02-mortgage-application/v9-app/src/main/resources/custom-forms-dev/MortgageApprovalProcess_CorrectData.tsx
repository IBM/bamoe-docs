import React, { useCallback, useEffect, useState } from 'react';
import { Card, CardBody, TextInput, FormGroup } from '@patternfly/react-core';
const Form__MortgageApprovalProcess_CorrectData: React.FC<any> = (
	props: any
) => {
	const [formApi, setFormApi] = useState<any>();
	const [application__amortization, set__application__amortization] =
		useState<number>();
	const [
		application__applicant__address,
		set__application__applicant__address,
	] = useState<string>('');
	const [
		application__applicant__annualincome,
		set__application__applicant__annualincome,
	] = useState<number>();
	const [
		application__applicant__creditrating,
		set__application__applicant__creditrating,
	] = useState<number>();
	const [application__applicant__name, set__application__applicant__name] =
		useState<string>('');
	const [application__applicant__ssn, set__application__applicant__ssn] =
		useState<number>();
	const [application__downpayment, set__application__downpayment] =
		useState<number>();
	const [application__errors__error, set__application__errors__error] =
		useState<string>('');
	const [application__mortgageamount, set__application__mortgageamount] =
		useState<number>();
	const [application__property__address, set__application__property__address] =
		useState<string>('');
	const [application__property__age, set__application__property__age] =
		useState<number>();
	const [application__property__locale, set__application__property__locale] =
		useState<string>('');
	const [
		application__property__saleprice,
		set__application__property__saleprice,
	] = useState<number>();
	/* Utility function that fills the form with the data received from the kogito runtime */
	const setFormData = (data) => {
		if (!data) {
			return;
		}
		set__application__amortization(data?.application?.amortization);
		set__application__applicant__address(
			data?.application?.applicant?.address ?? ''
		);
		set__application__applicant__annualincome(
			data?.application?.applicant?.annualincome
		);
		set__application__applicant__creditrating(
			data?.application?.applicant?.creditrating
		);
		set__application__applicant__name(data?.application?.applicant?.name ?? '');
		set__application__applicant__ssn(data?.application?.applicant?.ssn);
		set__application__downpayment(data?.application?.downpayment);
		set__application__errors__error(data?.application?.errors?.error ?? '');
		set__application__mortgageamount(data?.application?.mortgageamount);
		set__application__property__address(
			data?.application?.property?.address ?? ''
		);
		set__application__property__age(data?.application?.property?.age);
		set__application__property__locale(
			data?.application?.property?.locale ?? ''
		);
		set__application__property__saleprice(
			data?.application?.property?.saleprice
		);
	};
	/* Utility function to generate the expected form output as a json object */
	const getFormData = useCallback(() => {
		const formData: any = {};
		formData.application = {};
		formData.application.amortization = application__amortization;
		formData.application.applicant = {};
		formData.application.applicant.address = application__applicant__address;
		formData.application.applicant.annualincome =
			application__applicant__annualincome;
		formData.application.applicant.creditrating =
			application__applicant__creditrating;
		formData.application.applicant.name = application__applicant__name;
		formData.application.applicant.ssn = application__applicant__ssn;
		formData.application.downpayment = application__downpayment;
		formData.application.errors = {};
		formData.application.errors.error = application__errors__error;
		formData.application.mortgageamount = application__mortgageamount;
		formData.application.property = {};
		formData.application.property.address = application__property__address;
		formData.application.property.age = application__property__age;
		formData.application.property.locale = application__property__locale;
		formData.application.property.saleprice = application__property__saleprice;
		return formData;
	}, [
		application__amortization,
		application__applicant__address,
		application__applicant__annualincome,
		application__applicant__creditrating,
		application__applicant__name,
		application__applicant__ssn,
		application__downpayment,
		application__errors__error,
		application__mortgageamount,
		application__property__address,
		application__property__age,
		application__property__locale,
		application__property__saleprice,
	]);
	/* Utility function to validate the form on the 'beforeSubmit' Lifecycle Hook */
	const validateForm = useCallback(() => {}, []);
	/* Utility function to perform actions on the on the 'afterSubmit' Lifecycle Hook */
	const afterSubmit = useCallback((result) => {}, []);
	useEffect(() => {
		if (formApi) {
			/*
        Form Lifecycle Hook that will be executed before the form is submitted.
        Throwing an error will stop the form submit. Usually should be used to validate the form.
      */
			formApi.beforeSubmit = () => validateForm();
			/*
        Form Lifecycle Hook that will be executed after the form is submitted.
        It will receive a response object containing the `type` flag indicating if the submit has been successful and `info` with extra information about the submit result.
      */
			formApi.afterSubmit = (result) => afterSubmit(result);
			/* Generates the expected form output object to be posted */
			formApi.getFormData = () => getFormData();
		}
	}, [getFormData, validateForm, afterSubmit]);
	useEffect(() => {
		/*
      Call to the Kogito console form engine. It will establish the connection with the console embeding the form
      and return an instance of FormAPI that will allow hook custom code into the form lifecycle.
      The `window.Form.openForm` call expects an object with the following entries:
        - onOpen: Callback that will be called after the connection with the console is established. The callback
        will receive the following arguments:
          - data: the data to be bound into the form
          - ctx: info about the context where the form is being displayed. This will contain information such as the form JSON Schema, process/task, user...
    */
		const api = window.Form.openForm({
			onOpen: (data, context) => {
				setFormData(data);
			},
		});
		setFormApi(api);
	}, []);
	return (
		<div className={'pf-v5-c-form'}>
			<Card>
				<CardBody className='pf-v5-c-form'>
					<label>
						<b>Application</b>
					</label>
					<FormGroup
						fieldId={'uniforms-000c-0003'}
						label={'Amortization'}
						isRequired={false}>
						<TextInput
							type={'number'}
							name={'application.amortization'}
							isDisabled={false}
							id={'uniforms-000c-0003'}
							placeholder={''}
							step={1}
							value={application__amortization}
							onChange={(e, newValue) =>
								set__application__amortization(Number(newValue))
							}
						/>
					</FormGroup>
					<Card>
						<CardBody className='pf-v5-c-form'>
							<label>
								<b>Applicant</b>
							</label>
							<FormGroup
								fieldId={'uniforms-000c-0006'}
								label={'Address'}
								isRequired={false}>
								<TextInput
									name={'application.applicant.address'}
									id={'uniforms-000c-0006'}
									isDisabled={false}
									placeholder={''}
									type={'text'}
									value={application__applicant__address}
									onChange={(e, newValue) =>
										set__application__applicant__address(newValue)
									}
								/>
							</FormGroup>
							<FormGroup
								fieldId={'uniforms-000c-0008'}
								label={'Annualincome'}
								isRequired={false}>
								<TextInput
									type={'number'}
									name={'application.applicant.annualincome'}
									isDisabled={false}
									id={'uniforms-000c-0008'}
									placeholder={''}
									step={1}
									value={application__applicant__annualincome}
									onChange={(e, newValue) =>
										set__application__applicant__annualincome(Number(newValue))
									}
								/>
							</FormGroup>
							<FormGroup
								fieldId={'uniforms-000c-000a'}
								label={'Creditrating'}
								isRequired={false}>
								<TextInput
									type={'number'}
									name={'application.applicant.creditrating'}
									isDisabled={false}
									id={'uniforms-000c-000a'}
									placeholder={''}
									step={1}
									value={application__applicant__creditrating}
									onChange={(e, newValue) =>
										set__application__applicant__creditrating(Number(newValue))
									}
								/>
							</FormGroup>
							<FormGroup
								fieldId={'uniforms-000c-000b'}
								label={'Name'}
								isRequired={false}>
								<TextInput
									name={'application.applicant.name'}
									id={'uniforms-000c-000b'}
									isDisabled={false}
									placeholder={''}
									type={'text'}
									value={application__applicant__name}
									onChange={(e, newValue) =>
										set__application__applicant__name(newValue)
									}
								/>
							</FormGroup>
							<FormGroup
								fieldId={'uniforms-000c-000d'}
								label={'Ssn'}
								isRequired={false}>
								<TextInput
									type={'number'}
									name={'application.applicant.ssn'}
									isDisabled={false}
									id={'uniforms-000c-000d'}
									placeholder={''}
									step={1}
									value={application__applicant__ssn}
									onChange={(e, newValue) =>
										set__application__applicant__ssn(Number(newValue))
									}
								/>
							</FormGroup>
						</CardBody>
					</Card>
					<FormGroup
						fieldId={'uniforms-000c-000f'}
						label={'Downpayment'}
						isRequired={false}>
						<TextInput
							type={'number'}
							name={'application.downpayment'}
							isDisabled={false}
							id={'uniforms-000c-000f'}
							placeholder={''}
							step={1}
							value={application__downpayment}
							onChange={(e, newValue) =>
								set__application__downpayment(Number(newValue))
							}
						/>
					</FormGroup>
					<Card>
						<CardBody className='pf-v5-c-form'>
							<label>
								<b>Errors</b>
							</label>
							<FormGroup
								fieldId={'uniforms-000c-000i'}
								label={'Error'}
								isRequired={false}>
								<TextInput
									name={'application.errors.error'}
									id={'uniforms-000c-000i'}
									isDisabled={false}
									placeholder={''}
									type={'text'}
									value={application__errors__error}
									onChange={(e, newValue) =>
										set__application__errors__error(newValue)
									}
								/>
							</FormGroup>
						</CardBody>
					</Card>
					<FormGroup
						fieldId={'uniforms-000c-000k'}
						label={'Mortgageamount'}
						isRequired={false}>
						<TextInput
							type={'number'}
							name={'application.mortgageamount'}
							isDisabled={false}
							id={'uniforms-000c-000k'}
							placeholder={''}
							step={1}
							value={application__mortgageamount}
							onChange={(e, newValue) =>
								set__application__mortgageamount(Number(newValue))
							}
						/>
					</FormGroup>
					<Card>
						<CardBody className='pf-v5-c-form'>
							<label>
								<b>Property</b>
							</label>
							<FormGroup
								fieldId={'uniforms-000c-000n'}
								label={'Address'}
								isRequired={false}>
								<TextInput
									name={'application.property.address'}
									id={'uniforms-000c-000n'}
									isDisabled={false}
									placeholder={''}
									type={'text'}
									value={application__property__address}
									onChange={(e, newValue) =>
										set__application__property__address(newValue)
									}
								/>
							</FormGroup>
							<FormGroup
								fieldId={'uniforms-000c-000p'}
								label={'Age'}
								isRequired={false}>
								<TextInput
									type={'number'}
									name={'application.property.age'}
									isDisabled={false}
									id={'uniforms-000c-000p'}
									placeholder={''}
									step={1}
									value={application__property__age}
									onChange={(e, newValue) =>
										set__application__property__age(Number(newValue))
									}
								/>
							</FormGroup>
							<FormGroup
								fieldId={'uniforms-000c-000q'}
								label={'Locale'}
								isRequired={false}>
								<TextInput
									name={'application.property.locale'}
									id={'uniforms-000c-000q'}
									isDisabled={false}
									placeholder={''}
									type={'text'}
									value={application__property__locale}
									onChange={(e, newValue) =>
										set__application__property__locale(newValue)
									}
								/>
							</FormGroup>
							<FormGroup
								fieldId={'uniforms-000c-000s'}
								label={'Saleprice'}
								isRequired={false}>
								<TextInput
									type={'number'}
									name={'application.property.saleprice'}
									isDisabled={false}
									id={'uniforms-000c-000s'}
									placeholder={''}
									step={1}
									value={application__property__saleprice}
									onChange={(e, newValue) =>
										set__application__property__saleprice(Number(newValue))
									}
								/>
							</FormGroup>
						</CardBody>
					</Card>
				</CardBody>
			</Card>
		</div>
	);
};
export default Form__MortgageApprovalProcess_CorrectData;
