import React, { useCallback, useEffect, useState } from 'react';
import {
	Checkbox,
	FormGroup,
	Card,
	CardBody,
	TextInput,
} from '@patternfly/react-core';
const Form__MortgageApprovalProcess_IncreaseDownPayment: React.FC<any> = (
	props: any
) => {
	const [formApi, setFormApi] = useState<any>();
	const [incdownpayment, set__incdownpayment] = useState<boolean>(false);
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
		set__incdownpayment(data?.incdownpayment ?? false);
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
		formData.incdownpayment = incdownpayment;
		return formData;
	}, [incdownpayment]);
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
			<FormGroup fieldId='uniforms-000g-0001'>
				<Checkbox
					isChecked={incdownpayment}
					isDisabled={false}
					id={'uniforms-000g-0001'}
					name={'incdownpayment'}
					label={'Incdownpayment'}
					onChange={(e, newValue) => set__incdownpayment(newValue)}
				/>
			</FormGroup>
			<Card>
				<CardBody className='pf-v5-c-form'>
					<label>
						<b>Application</b>
					</label>
					<FormGroup
						fieldId={'uniforms-000g-0005'}
						label={'Amortization'}
						isRequired={false}>
						<TextInput
							type={'number'}
							name={'application.amortization'}
							isDisabled={true}
							id={'uniforms-000g-0005'}
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
								fieldId={'uniforms-000g-0008'}
								label={'Address'}
								isRequired={false}>
								<TextInput
									name={'application.applicant.address'}
									id={'uniforms-000g-0008'}
									isDisabled={true}
									placeholder={''}
									type={'text'}
									value={application__applicant__address}
									onChange={(e, newValue) =>
										set__application__applicant__address(newValue)
									}
								/>
							</FormGroup>
							<FormGroup
								fieldId={'uniforms-000g-000a'}
								label={'Annualincome'}
								isRequired={false}>
								<TextInput
									type={'number'}
									name={'application.applicant.annualincome'}
									isDisabled={true}
									id={'uniforms-000g-000a'}
									placeholder={''}
									step={1}
									value={application__applicant__annualincome}
									onChange={(e, newValue) =>
										set__application__applicant__annualincome(Number(newValue))
									}
								/>
							</FormGroup>
							<FormGroup
								fieldId={'uniforms-000g-000c'}
								label={'Creditrating'}
								isRequired={false}>
								<TextInput
									type={'number'}
									name={'application.applicant.creditrating'}
									isDisabled={true}
									id={'uniforms-000g-000c'}
									placeholder={''}
									step={1}
									value={application__applicant__creditrating}
									onChange={(e, newValue) =>
										set__application__applicant__creditrating(Number(newValue))
									}
								/>
							</FormGroup>
							<FormGroup
								fieldId={'uniforms-000g-000d'}
								label={'Name'}
								isRequired={false}>
								<TextInput
									name={'application.applicant.name'}
									id={'uniforms-000g-000d'}
									isDisabled={true}
									placeholder={''}
									type={'text'}
									value={application__applicant__name}
									onChange={(e, newValue) =>
										set__application__applicant__name(newValue)
									}
								/>
							</FormGroup>
							<FormGroup
								fieldId={'uniforms-000g-000f'}
								label={'Ssn'}
								isRequired={false}>
								<TextInput
									type={'number'}
									name={'application.applicant.ssn'}
									isDisabled={true}
									id={'uniforms-000g-000f'}
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
						fieldId={'uniforms-000g-000h'}
						label={'Downpayment'}
						isRequired={false}>
						<TextInput
							type={'number'}
							name={'application.downpayment'}
							isDisabled={true}
							id={'uniforms-000g-000h'}
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
								fieldId={'uniforms-000g-000k'}
								label={'Error'}
								isRequired={false}>
								<TextInput
									name={'application.errors.error'}
									id={'uniforms-000g-000k'}
									isDisabled={true}
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
						fieldId={'uniforms-000g-000m'}
						label={'Mortgageamount'}
						isRequired={false}>
						<TextInput
							type={'number'}
							name={'application.mortgageamount'}
							isDisabled={true}
							id={'uniforms-000g-000m'}
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
								fieldId={'uniforms-000g-000p'}
								label={'Address'}
								isRequired={false}>
								<TextInput
									name={'application.property.address'}
									id={'uniforms-000g-000p'}
									isDisabled={true}
									placeholder={''}
									type={'text'}
									value={application__property__address}
									onChange={(e, newValue) =>
										set__application__property__address(newValue)
									}
								/>
							</FormGroup>
							<FormGroup
								fieldId={'uniforms-000g-000r'}
								label={'Age'}
								isRequired={false}>
								<TextInput
									type={'number'}
									name={'application.property.age'}
									isDisabled={true}
									id={'uniforms-000g-000r'}
									placeholder={''}
									step={1}
									value={application__property__age}
									onChange={(e, newValue) =>
										set__application__property__age(Number(newValue))
									}
								/>
							</FormGroup>
							<FormGroup
								fieldId={'uniforms-000g-000s'}
								label={'Locale'}
								isRequired={false}>
								<TextInput
									name={'application.property.locale'}
									id={'uniforms-000g-000s'}
									isDisabled={true}
									placeholder={''}
									type={'text'}
									value={application__property__locale}
									onChange={(e, newValue) =>
										set__application__property__locale(newValue)
									}
								/>
							</FormGroup>
							<FormGroup
								fieldId={'uniforms-000g-000u'}
								label={'Saleprice'}
								isRequired={false}>
								<TextInput
									type={'number'}
									name={'application.property.saleprice'}
									isDisabled={true}
									id={'uniforms-000g-000u'}
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
export default Form__MortgageApprovalProcess_IncreaseDownPayment;
