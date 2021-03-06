package pa.iscde.codegenerator.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import pa.iscde.codegenerator.extensability.CodeGeneratorFunctionAddExtension;
import pa.iscde.codegenerator.extensability.CodeGeneratorFunctionAddExtension.CodePlacementLocation;
import pa.iscde.codegenerator.extensability.CodeStringGeneratorService;
import pa.iscde.codegenerator.extensability.CodeStringGeneratorService.AcessLevel;
import pa.iscde.codegenerator.extensability.CodeStringGeneratorService.IfType;
import pa.iscde.codegenerator.extensability.Functionality;
import pa.iscde.codegenerator.wrappers.Field;
import pa.iscde.codegenerator.wrappers.SimpleMethod;
import pa.iscde.search.model.MatchResult;
import pa.iscde.search.services.SearchService;
import pt.iscte.pidesco.extensibility.PidescoView;
import pt.iscte.pidesco.javaeditor.service.JavaEditorServices;
import pt.iscte.pidesco.projectbrowser.model.PackageElement;
import pt.iscte.pidesco.projectbrowser.service.ProjectBrowserServices;


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
	private ProjectBrowserServices browserService;
	private SearchService searchService;
	private Map<String, CodeStringGeneratorService> extensionServicesMap;
	private CodeGeneratorModel model;
	//View
	private Label errorLabel;
	private Composite mainViewArea;
	private Composite extensionAddArea;
	private SashForm mainSashForm;
	//Buttons
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
	private Button generateGetterSetterButton;
	private Button generateFieldButton;
	private Button generateSearchButton;

	@Override
	public void createContents(Composite parent, Map<String, Image> imageMap) {
		javaService = CodeGeneratorActivator.getInstance().getJavaEditorServices();
		currentCodeGeneratorService = CodeGeneratorActivator.getInstance().getCodeGeneratorService();
		searchService = CodeGeneratorActivator.getInstance().getSearchService();
		browserService = CodeGeneratorActivator.getInstance().getBrowserService();
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
					int endLine = getCorrectLine(model.getMethodEndLine());
					insertLine(file, setter, endLine);
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
					int endLine = getCorrectLine(model.getMethodEndLine());
					insertLine(file, setter, endLine);
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
					int endLine = getCorrectLine(model.getMethodEndLine());
					insertLine(file, setter, endLine);
					insertLine(file, getter, endLine);
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
					int lastConstructorEndLine = getCorrectLine(model.getMethodEndLine());
					String methodString = currentCodeGeneratorService.generateMethod(AcessLevel.PRIVATE, false, methodType, 
							method.getName(), method.getArguments());
					insertLine(file, methodString, lastConstructorEndLine);
				}
				else {
					setErrorMessage(NO_FILE_OPENED_ERROR);
				}
			}
		};
	}

	private SelectionListener setSearchListener() {
		return new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				CodeGeneratorResponse codeGeneratorResponse = model.getCodeGeneratorResponseWithDefaultOffset();
				File file = codeGeneratorResponse.getFile();
				if(file != null) {
					String selection = codeGeneratorResponse.getSelection();
					PackageElement root = browserService.getRootPackage();
					List<MatchResult> fieldResults = searchService.searchField(selection, root);
					List<MatchResult> methodResults = searchService.searchMethod(selection, root);
					int linesAdded = 0;
					for(MatchResult matchResult : fieldResults) {
						linesAdded = insertFieldComments(matchResult, linesAdded);
					}
					for(MatchResult matchResult : methodResults) {
						linesAdded = insertMethodComments(matchResult, linesAdded);
					}
				}
				else {
					setErrorMessage(NO_FILE_OPENED_ERROR);
				}
			}
		};
	}

	private int insertFieldComments(MatchResult matchResult, int linesAdded) {
		javaService.insertLine(matchResult.getFile(), currentCodeGeneratorService.generateCommentBeginString(), 
				matchResult.getLineNumber() + linesAdded - 1);
		linesAdded++;
		javaService.insertLine(matchResult.getFile(), currentCodeGeneratorService.generateCommentEndString(), 
				matchResult.getLineNumber() + linesAdded);
		linesAdded++;
		return linesAdded;
	}

	private int insertMethodComments(MatchResult matchResult, int linesAdded) {
		model.parse(matchResult.getFile(), matchResult.getNodeName(), null);
		javaService.insertLine(matchResult.getFile(), currentCodeGeneratorService.generateCommentBeginString(), 
				matchResult.getLineNumber() + linesAdded - 1);
		linesAdded++;
		javaService.insertLine(matchResult.getFile(), currentCodeGeneratorService.generateCommentEndString(), 
				model.getMethodEndLine() + 1);
		linesAdded++;
		return linesAdded;
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
			//Creates the new area for the extensions
			extensionAddArea = new Composite(mainSashForm, SWT.NONE);
			extensionAddArea.setLayout(new FillLayout(SWT.VERTICAL));
			//Creates new division for each add extension
			SashForm extensionSashForm = new SashForm(extensionAddArea, SWT.VERTICAL);
			extensionSashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			Label extLabel = new Label(extensionSashForm, SWT.NONE);
			extLabel.setText("Extension Functionalities:");
			for(IConfigurationElement element : elements) {
				try {
					CodeGeneratorFunctionAddExtension codeGeneratorFunctionAddExtension = (CodeGeneratorFunctionAddExtension) 
							element.createExecutableExtension("class");
					//Creates the base layout
					String extensionName = element.getAttribute("name");
					SashForm elementSashForm = new SashForm(extensionSashForm, SWT.VERTICAL);
					elementSashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
					Label label = new Label(elementSashForm, SWT.NONE);
					label.setText(extensionName + ":");
					//Adds the layout the extension made
					List<Functionality> functionalities = codeGeneratorFunctionAddExtension.getCodeGenerationContent();
					addFunctionalities(functionalities, elementSashForm);
					//Sets the relative weights for each element
					setSashWeights(elementSashForm, functionalities.size());
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
			setSashWeights(extensionSashForm, elements.length);
		}
	}

	private void addFunctionalities(List<Functionality> functionalities, SashForm viewArea) {
		for(Functionality functionality : functionalities) {
			Button button = new Button(viewArea, SWT.PUSH);
			button.setText(functionality.getButtonName());
			button.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					File file = model.getOpenedFile();
					if(file != null) {
						String generatedCode = functionality.getGeneratedCode();
						if(functionality.hasPlacementLocation()) {
							generateCode(file, generatedCode, functionality.getPlacementLocation());
						}
						else {
							model.parse(file);
							int line = functionality.getLine();
							if(line < model.getEndOfFileLine()) {
								insertLine(file, generatedCode, line);
							}
							else {
								setErrorMessage("Invalid line number");
							}
						}
					}
					else {
						setErrorMessage(NO_FILE_OPENED_ERROR);
					}
				}
			});
		}
	}

	//Sets the sashWeights to display the view with the proper distances
	private void setSashWeights(SashForm sashForm, int length) {
		int[] sashWeights = new int[length + 1];
		sashWeights[0] = 1;
		for(int i = 1; i<sashWeights.length; i++) {
			sashWeights[i] = 10;
		}
		sashForm.setWeights(sashWeights);
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
		mainSashForm = new SashForm(parent, SWT.HORIZONTAL);
		mainSashForm.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true));
		mainViewArea = new Composite(mainSashForm, SWT.NONE);
		mainViewArea.setLayout(new FillLayout(SWT.VERTICAL));
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
		generateSearchButton = new Button(mainViewArea, SWT.PUSH);
		generateSearchButton.setText("Comment all selection occurences");
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
		generateSearchButton.addSelectionListener(setSearchListener());
	}

	//Creates the label that will be responsible for showing any possible errors in the generation of code
	private void createErrorLabel() {
		Composite composite = new Composite(mainViewArea, SWT.NONE);
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));
		errorLabel = new Label(composite, SWT.NONE);
		errorLabel.setForeground(composite.getDisplay().getSystemColor(SWT.COLOR_RED));
	}

	//Gets end of file line if there's no method
	private int getCorrectLine(int methodEndLine) {
		int endLine = methodEndLine <= 0 ? model.getEndOfFileLine() - 1 : methodEndLine;
		return endLine;
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

	private void insertBindedVariable(File file, String selection, int line, List<Field> fields) {
		String bindedVariable = "";
		if(!fields.isEmpty()) {
			bindedVariable = currentCodeGeneratorService.generateBindedVariable(fields);
		}
		else {
			bindedVariable = currentCodeGeneratorService.generateBindedVariable(Arrays.asList(new Field("", selection)));
		}
		insertLine(file, bindedVariable, line + 1);
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
		if(!errorLabel.getText().equals(message)) {
			errorLabel.setText(message);
			errorLabel.redraw();
		}
	}

	//Inserts a string after the last field, or after the class initial line if there are no fields
	private void insertAfterField(File file, String constructor) {
		int fieldEndLine = model.getFieldEndLine();
		int endLine = fieldEndLine <= 0 ? model.getClassInitLine() : fieldEndLine;
		insertLine(file, constructor, endLine);
	}

	private void generateCode(File file, String generatedCode, CodePlacementLocation placementLocation) {
		switch(placementLocation) {
		case BEGIN_OF_CLASS:
			model.parse(file);
			insertLine(file, generatedCode, model.getClassInitLine());
			break;
		case END_OF_CLASS:
			model.parse(file);
			insertLine(file, generatedCode, model.getEndOfFileLine() - 1);
			break;
		case END_OF_FIELDS:
			model.parse(file);
			insertAfterField(file, generatedCode);
			break;
		case END_OF_CONSTRUCTOR:
			model.parse(file, model.getFileNameWithoutExtension(), null);
			insertLine(file, generatedCode, getCorrectLine(model.getMethodEndLine()));
			break;
		case REPLACEMENT_OF_SELECTION:
			int offset = model.getOffset();
			int selectionLength = model.getSelection().getLength();
			insertText(file, generatedCode, offset, selectionLength);
			break;
		case AFTER_SELECTION:
			offset = model.getCodeGeneratorResponseWithLengthOffset().getOffset();
			insertText(file, generatedCode, offset + 1, 0);
			break;
		case BEFORE_SELECTION:
			offset = model.getOffset();
			insertText(file, generatedCode, offset - 1, 0);
			break;
		case LINE_AFTER_SELECTION:
			ITextSelection selection = model.getSelection();
			insertLine(file, generatedCode, selection.getEndLine() + 1);
			break;
		case LINE_BEFORE_SELECTION:
			selection = model.getSelection();
			insertLine(file, generatedCode, selection.getStartLine() - 1);
			break;
		case CURSOR_POSITION:
			javaService.insertTextAtCursor(generatedCode);
			break;
		}
	}
}
