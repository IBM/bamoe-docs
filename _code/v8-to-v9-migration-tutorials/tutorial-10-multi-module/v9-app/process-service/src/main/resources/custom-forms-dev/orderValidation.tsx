import React, { useCallback, useEffect, useState } from 'react';
import {
	Card,
	CardBody,
	TextInput,
	FormGroup,
	DatePicker,
	Flex,
	FlexItem,
	InputGroup,
	TimePicker,
} from '@patternfly/react-core';
const Form__orderValidation: React.FC<any> = (props: any) => {
	const [formApi, setFormApi] = useState<any>();
	const [order__customerName, set__order__customerName] = useState<string>('');
	const [order__id, set__order__id] = useState<number>();
	const [order__orderDate, set__order__orderDate] = useState<string>();
	const [order__orderNumber, set__order__orderNumber] = useState<string>('');
	const [order__status, set__order__status] = useState<string>('');
	const [order__totalAmount, set__order__totalAmount] = useState<number>();
	/* Utility function that fills the form with the data received from the kogito runtime */
	const setFormData = (data) => {
		if (!data) {
			return;
		}
		set__order__customerName(data?.order?.customerName ?? '');
		set__order__id(data?.order?.id);
		set__order__orderDate(data?.order?.orderDate);
		set__order__orderNumber(data?.order?.orderNumber ?? '');
		set__order__status(data?.order?.status ?? '');
		set__order__totalAmount(data?.order?.totalAmount);
	};
	/* Utility function to generate the expected form output as a json object */
	const getFormData = useCallback(() => {
		const formData: any = {};
		formData.order = {};
		formData.order.customerName = order__customerName;
		formData.order.id = order__id;
		formData.order.orderDate = order__orderDate;
		formData.order.orderNumber = order__orderNumber;
		formData.order.status = order__status;
		formData.order.totalAmount = order__totalAmount;
		return formData;
	}, [
		order__customerName,
		order__id,
		order__orderDate,
		order__orderNumber,
		order__status,
		order__totalAmount,
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
	const parseDate = (date?: string): string => {
		if (!date) {
			return '';
		}
		const dateValue: Date = new Date(Date.parse(date));
		return dateValue.toISOString().slice(0, -14);
	};
	const parseTime = (date?: string): string => {
		if (!date) {
			return '';
		}
		const dateValue: Date = new Date(Date.parse(date));
		let isAm = true;
		let hours = dateValue.getHours();
		if (hours > 12) {
			hours %= 12;
			isAm = false;
		}
		let minutes = dateValue.getMinutes().toString();
		if (minutes.length == 1) {
			minutes = '0' + minutes;
		}
		return `${hours}:${minutes} ${isAm ? 'AM' : 'PM'}`;
	};
	return (
		<div className={'pf-v5-c-form'}>
			<Card>
				<CardBody className='pf-v5-c-form'>
					<label>
						<b>Order</b>
					</label>
					<FormGroup
						fieldId={'uniforms-0000-0002'}
						label={'Customer name'}
						isRequired={false}>
						<TextInput
							name={'order.customerName'}
							id={'uniforms-0000-0002'}
							isDisabled={false}
							placeholder={''}
							type={'text'}
							value={order__customerName}
							onChange={(e, newValue) => set__order__customerName(newValue)}
						/>
					</FormGroup>
					<FormGroup
						fieldId={'uniforms-0000-0004'}
						label={'Id'}
						isRequired={false}>
						<TextInput
							type={'number'}
							name={'order.id'}
							isDisabled={false}
							id={'uniforms-0000-0004'}
							placeholder={''}
							step={1}
							value={order__id}
							onChange={(e, newValue) => set__order__id(Number(newValue))}
						/>
					</FormGroup>
					<FormGroup
						fieldId={'uniforms-0000-0006'}
						label={'Order date'}
						isRequired={false}>
						<Flex direction={{ default: 'column' }} id={'uniforms-0000-0006'}>
							<FlexItem>
								<InputGroup style={{ background: 'transparent' }}>
									<DatePicker
										id={'date-picker-uniforms-0000-0006'}
										isDisabled={false}
										name={'order.orderDate'}
										onChange={(e, newDate) => {
											set__order__orderDate((prev) => {
												if (newDate) {
													const newDate = new Date(newDate);
													const time = parseTime(prev);
													if (time !== '') {
														newDate.setHours(
															parseInt(time && time.split(':')[0])
														);
														newDate.setMinutes(
															parseInt(time && time.split(':')[1].split(' ')[0])
														);
													}
													return newDate.toISOString();
												}
												return prev;
											});
										}}
										value={parseDate(order__orderDate)}
									/>
									<TimePicker
										id={'time-picker-uniforms-0000-0006'}
										isDisabled={false}
										name={'order.orderDate'}
										onChange={(e, time, hours?, minutes?) => {
											set__order__orderDate((prev) => {
												if (prev) {
													const newDate = new Date(Date.parse(prev));
													if (hours && minutes) {
														newDate.setHours(hours);
														newDate.setMinutes(minutes);
													} else if (time !== '') {
														const localeHours = parseInt(
															time && time.split(':')[0]
														);
														const localeMinutes = parseInt(
															time && time.split(':')[1].split(' ')[0]
														);
														if (!isNaN(localeHours) && !isNaN(localeMinutes)) {
															newDate.setHours(localeHours);
															newDate.setMinutes(localeMinutes);
														}
													}
													return newDate.toISOString();
												}
												return prev;
											});
										}}
										style={{ width: '120px' }}
										time={parseTime(order__orderDate)}
									/>
								</InputGroup>
							</FlexItem>
						</Flex>
					</FormGroup>
					<FormGroup
						fieldId={'uniforms-0000-0007'}
						label={'Order number'}
						isRequired={false}>
						<TextInput
							name={'order.orderNumber'}
							id={'uniforms-0000-0007'}
							isDisabled={false}
							placeholder={''}
							type={'text'}
							value={order__orderNumber}
							onChange={(e, newValue) => set__order__orderNumber(newValue)}
						/>
					</FormGroup>
					<FormGroup
						fieldId={'uniforms-0000-0008'}
						label={'Status'}
						isRequired={false}>
						<TextInput
							name={'order.status'}
							id={'uniforms-0000-0008'}
							isDisabled={false}
							placeholder={''}
							type={'text'}
							value={order__status}
							onChange={(e, newValue) => set__order__status(newValue)}
						/>
					</FormGroup>
					<FormGroup
						fieldId={'uniforms-0000-000a'}
						label={'Total amount'}
						isRequired={false}>
						<TextInput
							type={'number'}
							name={'order.totalAmount'}
							isDisabled={false}
							id={'uniforms-0000-000a'}
							placeholder={''}
							step={0.01}
							value={order__totalAmount}
							onChange={(e, newValue) =>
								set__order__totalAmount(Number(newValue))
							}
						/>
					</FormGroup>
				</CardBody>
			</Card>
		</div>
	);
};
export default Form__orderValidation;
