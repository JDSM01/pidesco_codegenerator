package pt.iscte.pidesco.codegenerator.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import pt.iscte.pidesco.codegenerator.extensability.CodeGeneratorFunctionAddExtension;
import pt.iscte.pidesco.codegenerator.extensability.CodeStringGeneratorService;
import pt.iscte.pidesco.codegenerator.extensability.CodeStringGeneratorService.AcessLevel;
import pt.iscte.pidesco.codegenerator.extensability.CodeStringGeneratorService.IfType;
import pt.iscte.pidesco.codegenerator.wrappers.Field;
import pt.iscte.pidesco.codegenerator.wrappers.SimpleMethod;
import pt.iscte.pidesco.extensibility.PidescoView;
import pt.iscte.pidesco.javaeditor.service.JavaEditorServices;


/**
 * This class is responsible for handling everything related to the view. This includes clicks, selections, creation of visual
 * objects and listeners.
 * @author D01
 *
 */
public class CodeGeneratorView implements PidescoView{
	private final static String ORIGINAL_TAG = "original";
	private final static String NO_FILE_OPENED_ERROR = "There's no open file";
	private final static int INITIAL_UNIQUE_NAME = 1;
	private JavaEditorServices javaService;
	private CodeStringGeneratorService currentCodeGeneratorService;
	private CodeGeneratorModel model;
	private Button generateSetterButton;
	private Button generateGetterButton;
	private Button generateMethodButton;
	private Button generateConstructorWithBindingButton;
	private Button generateConstructorButton;
	private Button bindFieldButton;
	private Button bindFieldVariableButton;
	private Button generateIfNullButton;
	private Button generateIfNotNullButton;
	private Button generateIfButton;
	private Button generateVariableNameButton;
	private Map<String, CodeStringGeneratorService> extensionServicesMap;
	private Label label;
	private Button generateGetterSetterButton;
	private Button generateFieldButton;
	private Composite mainViewArea;
	private Composite extensionAddArea;

	@Override
	public void createContents(Composite parent, Map<String, Image> imageMap) {
		javaService = CodeGeneratorActivator.getInstance().getJavaEditorServices();
		currentCodeGeneratorService = CodeGeneratorActivator.getInstance().getCodeGeneratorService();

		model = new CodeGeneratorModel(javaService);
		createLayout(parent);
		createButtons();
		setListeners();
		createExtensions();
		createErrorLabel();
	}

	private SelectionAdapter setGetterListener() {
		return new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CodeGeneratorResponse codeGeneratorResponse = model.getCodeGeneratorResponseWithDefaultOffset();
				File file = codeGeneratorResponse.getFile();
				if(file != null) {
					Field field = model.getTypeAndVariableName(codeGeneratorResponse.getSelection());
					String fileName = model.getFileNameWithoutExtension(file.getName());
					model.parse(file, fileName, null);
					String setter = currentCodeGeneratorService.generateGetter(field.getType(), field.getName());
					int offset = getCorrectOffset(model.getConstructorEndOffset());
					insertText(file, setter, offset, 0);
				}
				else {
					setErrorMessage(NO_FILE_OPENED_ERROR);
				}
			}
		};
	}

	private SelectionAdapter setSetterListener() {
		return new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CodeGeneratorResponse codeGeneratorResponse = model.getCodeGeneratorResponseWithDefaultOffset();
				File file = codeGeneratorResponse.getFile();
				if(file != null) {
					Field field = model.getTypeAndVariableName(codeGeneratorResponse.getSelection());
					String fileName = model.getFileNameWithoutExtension(file.getName());
					model.parse(file, fileName, null);
					String setter = currentCodeGeneratorService.generateSetter(field.getType(), field.getName());
					int offset = getCorrectOffset(model.getConstructorEndOffset());
					insertText(file, setter, offset, 0);
				}
				else {
					setErrorMessage(NO_FILE_OPENED_ERROR);
				}
			}
		};
	}

	private SelectionListener setGetterSetterListener() {
		return new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CodeGeneratorResponse codeGeneratorResponse = model.getCodeGeneratorResponseWithDefaultOffset();
				File file = codeGeneratorResponse.getFile();
				if(file != null) {
					Field field = model.getTypeAndVariableName(codeGeneratorResponse.getSelection());
					String fileName = model.getFileNameWithoutExtension(file.getName());
					model.parse(file, fileName, null);
					String setter = currentCodeGeneratorService.generateSetter(field.getType(), field.getName());
					String getter = currentCodeGeneratorService.generateGetter(field.getType(), field.getName());
					int offset = getCorrectOffset(model.getConstructorEndOffset());
					insertText(file, setter, offset, 0);
					insertText(file, getter, offset, 0);
				}
				else {
					setErrorMessage(NO_FILE_OPENED_ERROR);
				}
			}
		};
	}

	private SelectionAdapter setCreateConstructorListener() {
		return new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CodeGeneratorResponse codeGeneratorResponse = model.getCodeGeneratorResponseWithDefaultOffset();
				File file = codeGeneratorResponse.getFile();
				if(file != null) {
					model.parse(file);
					String selection = codeGeneratorResponse.getSelection();
					List<Field> fields = model.getTypeAndVariableNameToList(selection, ";");
					String fileName = model.getFileNameWithoutExtension(file.getName());
					String constructor = currentCodeGeneratorService.generateConstructor(fileName, fields);
					insertAfterField(file, constructor);
				}
				else {
					setErrorMessage(NO_FILE_OPENED_ERROR);
				}
			}
		};
	}

	private SelectionAdapter setBindFieldListener() {
		return new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CodeGeneratorResponse response = model.getCodeGeneratorResponseWithEndLine();
				File file = response.getFile();
				if (file != null) {
					String selection = response.getSelection();
					List<Field> fields = model.getTypeAndVariableNameToList(selection,",");
					model.parse(file);
					String bindedField = currentCodeGeneratorService.generateField(AcessLevel.PRIVATE, false, true, fields);
					insertBindedVariable(file, selection, response.getOffset(), fields);
					insertAfterField(file, bindedField);
				}
				else {
					setErrorMessage(NO_FILE_OPENED_ERROR);
				}
			}
		};
	}

	private SelectionAdapter setBindListener() {
		return new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CodeGeneratorResponse response = model.getCodeGeneratorResponseWithEndLine();
				File file = response.getFile();
				if (file != null) {
					String selection = response.getSelection();
					List<Field> fields = model.getTypeAndVariableNameToList(selection,",");
					insertBindedVariable(file, selection, response.getOffset(), fields);
				}
				else {
					setErrorMessage(NO_FILE_OPENED_ERROR);
				}
			}
		};
	}

	private SelectionListener setFieldListener() {
		return new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CodeGeneratorResponse response = model.getCodeGeneratorResponseWithDefaultOffset();
				File file = response.getFile();
				if (file != null) {
					String selection = response.getSelection();
					List<Field> fields = model.getTypeAndVariableNameToList(selection,",");
					model.parse(file);
					String bindedField = currentCodeGeneratorService.generateField(AcessLevel.PRIVATE, false, true, fields);
					insertAfterField(file, bindedField);
				}
				else {
					setErrorMessage(NO_FILE_OPENED_ERROR);
				}
			}
		};
	}


	private SelectionAdapter setConstructorWithBindingListnener() {
		return new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CodeGeneratorResponse codeGeneratorResponse = model.getCodeGeneratorResponseWithDefaultOffset();
				File file = codeGeneratorResponse.getFile();
				if(file != null) {
					model.parse(file);
					String selection = codeGeneratorResponse.getSelection();
					List<Field> fields = model.getTypeAndVariableNameToList(selection, ";");
					String fileName = model.getFileNameWithoutExtension(file.getName());
					String constructor = currentCodeGeneratorService.generateConstructorWithBinding(fileName, fields);
					insertAfterField(file, constructor);	
				}
				else {
					setErrorMessage(NO_FILE_OPENED_ERROR);
				}
			}
		};
	}

	private SelectionAdapter setIfNullListener() {
		return new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				insertIf(IfType.NULL);
			}
		};
	}

	private SelectionAdapter setIfNotNullListener() {
		return new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				insertIf(IfType.NOT_NULL);
			}
		};
	}

	private SelectionAdapter setIfListener() {
		return new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				insertIf(IfType.CONDITION);
			}
		};
	}

	private SelectionAdapter setVariableNameListener() {
		return new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CodeGeneratorResponse response = model.getCodeGeneratorResponseWithLengthOffset();
				File file = response.getFile();
				if (file != null) {
					String variableName = currentCodeGeneratorService.generateVariableName(response.getSelection(), 
							CodeStringGeneratorService.JAVA, false);
					insertText(file, variableName, response.getOffset(), 0);
				}
				else {
					setErrorMessage(NO_FILE_OPENED_ERROR);
				}
			}
		};
	}

	private SelectionAdapter setMethodListener() {
		return new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CodeGeneratorResponse codeGeneratorResponse = model.getCodeGeneratorResponseWithDefaultOffset();
				File file = codeGeneratorResponse.getFile();
				if(file != null) {
					String selection = codeGeneratorResponse.getSelection();
					SimpleMethod method = model.getMethodNameAndArguments(selection);
					String fileName = model.getFileNameWithoutExtension(file.getName());
					model.parse(file, fileName, selection.replaceAll(";", "").replaceAll(" ", ""));
					String methodType = model.getMethodType();
					int lastConstructorEndOffset = getCorrectOffset(model.getConstructorEndOffset());
					String setter = currentCodeGeneratorService.generateMethod(AcessLevel.PRIVATE, false, methodType, 
							method.getName(), method.getArguments());
					insertText(file, setter, lastConstructorEndOffset, 0);
				}
				else {
					setErrorMessage(NO_FILE_OPENED_ERROR);
				}
			}
		};
	}

	//Handles the creation of all extensions
	private void createExtensions() {
		createFunctionReplacementExtension();
		createFunctionAddExtension();
	}

	private void createFunctionReplacementExtension() {
		IConfigurationElement[] elements = model.getFunctionReplacementExtension();
		if(elements.length > 0) {
			extensionServicesMap = new HashMap<>();	//Create hashmap to handle the different services extensions
			extensionServicesMap.put(ORIGINAL_TAG, currentCodeGeneratorService); //Saves the current service

			//Radio Button creation
			Composite composite = new Composite(mainViewArea, SWT.NONE);
			composite.setLayout(new RowLayout(SWT.HORIZONTAL));
			new Label(composite, SWT.NONE).setText("Extensions: ");
			createRadioButton(composite, ORIGINAL_TAG, true);

			//To avoid not unique extension names
			List<String> extensionsNames = new ArrayList<>();
			extensionsNames.add(ORIGINAL_TAG);
			for(IConfigurationElement element : elements) {
				String name = element.getAttribute("name");
				String uniqueName = getUniqueName(extensionsNames, name, INITIAL_UNIQUE_NAME);
				extensionsNames.add(uniqueName);
				try {
					CodeStringGeneratorService codeGeneratorController = (CodeStringGeneratorService) element.createExecutableExtension("class");
					extensionServicesMap.put(uniqueName, codeGeneratorController);
					createRadioButton(composite, uniqueName, false);
				} catch (CoreException e1) {
					e1.printStackTrace();
				}
			} 
		}
	}
	
	private void createFunctionAddExtension() {
		IConfigurationElement[] elements = model.getFunctionAddExtension();
		if(elements.length > 0) {
			Label extLabel = new Label(extensionAddArea, SWT.NONE);
			extLabel.setText("Extension Functionalities:");
		}
		//Creates new division for each add extension
		SashForm sashForm = new SashForm(extensionAddArea, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		for(IConfigurationElement element : elements) {
			try {
				CodeGeneratorFunctionAddExtension codeGeneratorFunctionAddExtension = (CodeGeneratorFunctionAddExtension) 
						element.createExecutableExtension("class");
				//Creates the base layout
				String extensionName = element.getAttribute("name");
				Composite extensionComposite = new Composite(sashForm, SWT.NONE);
				extensionComposite.setLayout(new FillLayout(SWT.VERTICAL));
				Label label = new Label(extensionComposite, SWT.NONE);
				label.setText(extensionName + ":");
				//Adds the layout the extension made
				codeGeneratorFunctionAddExtension.createCodeGenerationContent(extensionComposite);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	//Returns an unique name for each extension so that there's no extensions with the same name
	private String getUniqueName(List<String> extensionsNames, String name, int notUniqueNumber) {
		String uniqueName = name;
		for(String extensionName : extensionsNames) {
			if(extensionName.equals(name)) {
				if(notUniqueNumber != INITIAL_UNIQUE_NAME) {
					name = name.substring(0, name.length() - String.valueOf(notUniqueNumber - 1).length()); //removes previously added numbers
				}
				uniqueName = name + notUniqueNumber; //Adds number to the name
				uniqueName = getUniqueName(extensionsNames, uniqueName, notUniqueNumber + 1); //Checks if the new name is already being used
			}
		}
		return uniqueName;
	}
	
	//Creates the initial layout
	private void createLayout(Composite parent) {
		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true));
		mainViewArea = new Composite(sashForm, SWT.NONE);
		mainViewArea.setLayout(new FillLayout(SWT.VERTICAL));
		extensionAddArea = new Composite(sashForm, SWT.NONE);
		extensionAddArea.setLayout(new FillLayout(SWT.VERTICAL));
	}

	//Create and set the text of the buttons
	private void createButtons() {
		Label label = new Label(mainViewArea, SWT.NONE);
		label.setText("Base functionalities:");
		generateVariableNameButton = new Button(mainViewArea, SWT.PUSH);
		generateVariableNameButton.setText("Generate Variable Name");
		generateIfButton = new Button(mainViewArea, SWT.PUSH);
		generateIfButton.setText("Generate If");
		generateIfNotNullButton = new Button(mainViewArea, SWT.PUSH);
		generateIfNotNullButton.setText("Generate If not null");
		generateIfNullButton = new Button(mainViewArea, SWT.PUSH);
		generateIfNullButton.setText("Generate If null");
		bindFieldVariableButton = new Button(mainViewArea, SWT.PUSH);
		bindFieldVariableButton.setText("Bind Variable with Field");
		bindFieldButton = new Button(mainViewArea, SWT.PUSH);
		bindFieldButton.setText("Bind Variable");
		generateConstructorButton = new Button(mainViewArea, SWT.PUSH);
		generateConstructorButton.setText("Create constructor");
		generateSetterButton = new Button(mainViewArea, SWT.PUSH);
		generateSetterButton.setText("Create Setter");
		generateGetterButton = new Button(mainViewArea, SWT.PUSH);
		generateGetterButton.setText("Create Getter");
		generateGetterSetterButton = new Button(mainViewArea, SWT.PUSH);
		generateGetterSetterButton.setText("Create Getter and Setter");
		generateMethodButton = new Button(mainViewArea, SWT.PUSH);
		generateMethodButton.setText("Create Method");
		generateConstructorWithBindingButton = new Button(mainViewArea, SWT.PUSH);
		generateConstructorWithBindingButton.setText("Create constructor and bind");
		generateFieldButton = new Button(mainViewArea, SWT.PUSH);
		generateFieldButton.setText("Create Field");
	}

	private void createRadioButton(Composite composite, String name, boolean select) {
		Button radioButton = new Button(composite, SWT.RADIO);
		radioButton.setText(name);
		radioButton.setSelection(select);
		radioButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				currentCodeGeneratorService = extensionServicesMap.get(name); //changes the service being used to generate the code
			}});
	}

	//Adds listeners to every button created 
	private void setListeners() {
		generateVariableNameButton.addSelectionListener(setVariableNameListener());
		generateIfButton.addSelectionListener(setIfListener());
		generateIfNotNullButton.addSelectionListener(setIfNotNullListener());
		generateIfNullButton.addSelectionListener(setIfNullListener());
		bindFieldVariableButton.addSelectionListener(setBindFieldListener());
		bindFieldButton.addSelectionListener(setBindListener());
		generateConstructorButton.addSelectionListener(setCreateConstructorListener());
		generateSetterButton.addSelectionListener(setSetterListener());
		generateGetterButton.addSelectionListener(setGetterListener());
		generateGetterSetterButton.addSelectionListener(setGetterSetterListener());
		generateMethodButton.addSelectionListener(setMethodListener());
		generateConstructorWithBindingButton.addSelectionListener(setConstructorWithBindingListnener());
		generateFieldButton.addSelectionListener(setFieldListener());
	}

	//Creates the label that will be responsible for showing any possible errors in the generation of code
	private void createErrorLabel() {
		Composite composite = new Composite(mainViewArea, SWT.NONE);
		composite.setLayout(new RowLayout(SWT.HORIZONTAL));
		label = new Label(composite, SWT.NONE);
		label.setForeground(composite.getDisplay().getSystemColor(SWT.COLOR_RED));
	}

	//Gets end of file offset if there's no constructor
	private int getCorrectOffset(int constructorEndOffset) {
		int offset = constructorEndOffset == 0 ? model.getEndOfFileOffset() - 1 : constructorEndOffset + 2;
		return offset;
	}

	private void insertIf(IfType type) {
		CodeGeneratorResponse response = model.getCodeGeneratorResponseWithDefaultOffset();
		File file = response.getFile();
		if (file != null) {
			String selection = response.getSelection();
			String generatedIf = currentCodeGeneratorService.generateIfCondition(selection, type);
			insertText(file, generatedIf, response.getOffset(), selection.length());
		}
	}

	private void insertBindedVariable(File file, String selection, int offset, List<Field> fields) {
		String bindedVariable = "";
		if(!fields.isEmpty()) {
			bindedVariable = currentCodeGeneratorService.generateBindedVariable(fields);
		}
		else {
			bindedVariable = currentCodeGeneratorService.generateBindedVariable(Arrays.asList(new Field("", selection)));
		}
		insertLine(file, bindedVariable, offset + 1);
	}

	//Uses the JavaEditorServices to insert a string or set an error message if there's an error
	private void insertLine(File file, String generatedString, int line) {
		String error = model.getError(generatedString, line);
		if(error != null) {
			setErrorMessage(error);
		} else {
			setErrorMessage("");
			javaService.insertLine(file, generatedString, line);
		}
	}

	//Uses the JavaEditorServices to insert a string or set an error message if there's an error
	private void insertText(File file, String generatedString, int offset, int length) {
		String error = model.getError(generatedString, offset);
		if(error != null) {
			setErrorMessage(error);
		} else {
			setErrorMessage("");
			javaService.insertText(file, generatedString, offset, length);
		}
	}

	//Sets an error message in the view
	private void setErrorMessage(String message) {
		if(!label.getText().equals(message)) {
			label.setText(message);
			label.requestLayout();
		}
	}

	//Inserts a string after the last field, or after the class initial line if there are no fields
	private void insertAfterField(File file, String constructor) {
		int fieldEndOffset = model.getFieldEndLine();
		int offset = fieldEndOffset == 0 ? model.getClassInitLine() : fieldEndOffset;
		insertLine(file, constructor, offset);	

	}
}
