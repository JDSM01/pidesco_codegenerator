package pa.iscde.codegenerator.internal;

import java.io.File;
import java.util.List;

import pa.iscde.codegenerator.extensability.CodeStringGeneratorService;
import pa.iscde.codegenerator.extensability.CodeStringGeneratorService.AcessLevel;
import pa.iscde.codegenerator.extensability.CodeStringGeneratorService.IfType;
import pa.iscde.codegenerator.service.CodeGeneratorService;
import pa.iscde.codegenerator.wrappers.Field;
import pa.iscde.codegenerator.wrappers.Regex;
import pt.iscte.pidesco.javaeditor.service.JavaEditorServices;

public class CodeGeneratorServiceImpl implements CodeGeneratorService{
	private final JavaEditorServices javaService;
	private final CodeStringGeneratorService codeStringGeneratorService;
	private final CodeGeneratorModel codeGeneratorModel;

	public CodeGeneratorServiceImpl(JavaEditorServices javaService, CodeStringGeneratorService codeStringGeneratorService,
			CodeGeneratorModel codeGeneratorModel) {
		this.javaService = javaService;
		this.codeStringGeneratorService = codeStringGeneratorService;
		this.codeGeneratorModel = codeGeneratorModel;
	}

	@Override
	public void generateVariableName(String convertFrom, String languageType, boolean isStatic, int line) {
		File file = codeGeneratorModel.getOpenedFile();
		codeGeneratorModel.parse(file, null, convertFrom, line);
		String variableName = codeStringGeneratorService.generateVariableName(convertFrom, languageType, isStatic);
		javaService.insertText(file, variableName, codeGeneratorModel.getVariableOffset(), 0);
	}

	@Override
	public void generateVariableName(String convertFrom, Regex regex, int line) {
		File file = codeGeneratorModel.getOpenedFile();
		codeGeneratorModel.parse(file, null, convertFrom, line);
		String variableName = codeStringGeneratorService.generateVariableName(convertFrom, regex);
		javaService.insertText(file, variableName, codeGeneratorModel.getVariableOffset(), 0);
	}

	@Override
	public void generateIfConditionInLine(String condition, IfType ifType, int line) {
		File file = codeGeneratorModel.getOpenedFile();
		String ifString = codeStringGeneratorService.generateIfCondition(condition, ifType);
		javaService.insertLine(file, ifString, line);
	}

	@Override
	public void generateIfConditionInLine(String condition, String body, IfType ifType, int line) {
		File file = codeGeneratorModel.getOpenedFile();
		String ifString = codeStringGeneratorService.generateIfCondition(condition, body, ifType);
		javaService.insertLine(file, ifString, line);
	}

	@Override
	public void generateIfConditionInOffset(String condition, IfType ifType, int offset) {
		File file = codeGeneratorModel.getOpenedFile();
		String ifString = codeStringGeneratorService.generateIfCondition(condition, ifType);
		javaService.insertLine(file, ifString, offset);
	}

	@Override
	public void generateIfConditionInOffset(String condition, String body, IfType ifType, int offset) {
		File file = codeGeneratorModel.getOpenedFile();
		String ifString = codeStringGeneratorService.generateIfCondition(condition, body, ifType);
		javaService.insertLine(file, ifString, offset);
	}

	@Override
	public void generateIfConditionInOffset(String condition, IfType ifType) {
		String ifString = codeStringGeneratorService.generateIfCondition(condition, ifType);
		javaService.insertTextAtCursor(ifString);
	}

	@Override
	public void generateIfConditionInOffset(String condition, String body, IfType ifType) {
		String ifString = codeStringGeneratorService.generateIfCondition(condition, body, ifType);
		javaService.insertTextAtCursor(ifString);
	}

	@Override
	public void generateBindedVariable(List<Field> fields) {
		File file = codeGeneratorModel.getOpenedFile();
		codeGeneratorModel.parse(file, codeGeneratorModel.getFileNameWithoutExtension(), null);
		String bindedVariable = codeStringGeneratorService.generateBindedVariable(fields);
		int endLine = codeGeneratorModel.getMethodEndLine();
		if(endLine != 0) {
			javaService.insertText(file, bindedVariable, endLine, 0);
		}
	}

	@Override
	public void generateConstructor(String className, List<Field> arguments) {
		File file = codeGeneratorModel.getOpenedFile();
		codeGeneratorModel.parse(file);
		String constructor = codeStringGeneratorService.generateConstructor(className, arguments);
		insertAfterField(file, constructor);
	}

	@Override
	public void generateConstructor(String className, List<Field> arguments, String input) {
		File file = codeGeneratorModel.getOpenedFile();
		codeGeneratorModel.parse(file);
		String constructor = codeStringGeneratorService.generateConstructor(className, arguments, input);
		insertAfterField(file, constructor);
	}

	@Override
	public void generateConstructor(String className, List<Field> arguments, int line) {
		File file = codeGeneratorModel.getOpenedFile();
		codeGeneratorModel.parse(file);
		String constructor = codeStringGeneratorService.generateConstructor(className, arguments);
		javaService.insertLine(file, constructor, line);
	}

	@Override
	public void generateConstructor(String className, List<Field> arguments, String input, int line) {
		File file = codeGeneratorModel.getOpenedFile();
		codeGeneratorModel.parse(file);
		String constructor = codeStringGeneratorService.generateConstructor(className, arguments, input);
		javaService.insertLine(file, constructor, line);
	}

	@Override
	public void generateConstructorWithBinding(String className, List<Field> arguments) {
		File file = codeGeneratorModel.getOpenedFile();
		codeGeneratorModel.parse(file);
		String constructor = codeStringGeneratorService.generateConstructorWithBinding(className, arguments);
		insertAfterField(file, constructor);
	}

	@Override
	public void generateConstructorWithBinding(String className, List<Field> arguments, String input) {
		File file = codeGeneratorModel.getOpenedFile();
		codeGeneratorModel.parse(file);
		String constructor = codeStringGeneratorService.generateConstructorWithBinding(className, arguments, input);
		insertAfterField(file, constructor);
	}

	@Override
	public void generateConstructorWithBinding(String className, List<Field> arguments, int line) {
		File file = codeGeneratorModel.getOpenedFile();
		codeGeneratorModel.parse(file);
		String constructor = codeStringGeneratorService.generateConstructorWithBinding(className, arguments);
		javaService.insertLine(file, constructor, line);
	}

	@Override
	public void generateConstructorWithBinding(String className, List<Field> arguments, String input, int line) {
		File file = codeGeneratorModel.getOpenedFile();
		codeGeneratorModel.parse(file);
		String constructor = codeStringGeneratorService.generateConstructorWithBinding(className, arguments, input);
		javaService.insertLine(file, constructor, line);
	}

	@Override
	public void generateSetter(String variableType, String variableName) {
		File file = codeGeneratorModel.getOpenedFile();
		codeGeneratorModel.parse(file, codeGeneratorModel.getFileNameWithoutExtension(), null);
		String setter = codeStringGeneratorService.generateSetter(variableType, variableName);
		int endLine = getCorrectLine(codeGeneratorModel.getMethodEndLine());
		javaService.insertLine(file, setter, endLine);
	}

	@Override
	public void generateSetter(String variableType, String variableName, int line) {
		File file = codeGeneratorModel.getOpenedFile();
		String setter = codeStringGeneratorService.generateSetter(variableType, variableName);
		javaService.insertLine(file, setter, line);
	}

	@Override
	public void generateSetter(String variableType, String variableName, String methodName) {
		File file = codeGeneratorModel.getOpenedFile();
		codeGeneratorModel.parse(file, codeGeneratorModel.getFileNameWithoutExtension(), null);
		String setter = codeStringGeneratorService.generateSetter(variableType, variableName, methodName);
		int endLine = getCorrectLine(codeGeneratorModel.getMethodEndLine());
		javaService.insertLine(file, setter, endLine);
	}

	@Override
	public void generateSetter(String variableType, String variableName, String methodName, int line) {
		File file = codeGeneratorModel.getOpenedFile();
		String setter = codeStringGeneratorService.generateSetter(variableType, variableName, methodName);
		javaService.insertLine(file, setter, line);
	}

	@Override
	public void generateGetter(String variableType, String variableName) {
		File file = codeGeneratorModel.getOpenedFile();
		codeGeneratorModel.parse(file, codeGeneratorModel.getFileNameWithoutExtension(), null);
		String getter = codeStringGeneratorService.generateGetter(variableType, variableName);
		int endLine = getCorrectLine(codeGeneratorModel.getMethodEndLine());
		javaService.insertLine(file, getter, endLine);
	}

	@Override
	public void generateGetter(String variableType, String variableName, int line) {
		File file = codeGeneratorModel.getOpenedFile();
		String getter = codeStringGeneratorService.generateGetter(variableType, variableName);
		javaService.insertLine(file, getter, line);
	}

	@Override
	public void generateGetter(String variableType, String variableName, String methodName) {
		File file = codeGeneratorModel.getOpenedFile();
		codeGeneratorModel.parse(file, codeGeneratorModel.getFileNameWithoutExtension(), null);
		String getter = codeStringGeneratorService.generateGetter(variableType, variableName, methodName);
		int endLine = getCorrectLine(codeGeneratorModel.getMethodEndLine());
		javaService.insertLine(file, getter, endLine);
	}

	@Override
	public void generateGetter(String variableType, String variableName, String methodName, int line) {
		File file = codeGeneratorModel.getOpenedFile();
		String getter = codeStringGeneratorService.generateGetter(variableType, variableName, methodName);
		javaService.insertLine(file, getter, line);
	}

	@Override
	public void generateField(AcessLevel acessLevel, boolean isStatic, boolean isFinal, List<Field> fields) {
		File file = codeGeneratorModel.getOpenedFile();
		codeGeneratorModel.parse(file);
		String field = codeStringGeneratorService.generateField(acessLevel, isStatic, isFinal, fields);
		insertAfterField(file, field);
	}

	@Override
	public void generateMethod(AcessLevel acessLevel, boolean isStatic, String returnType, String methodName,
			List<Field> arguments) {
		File file = codeGeneratorModel.getOpenedFile();
		codeGeneratorModel.parse(file, codeGeneratorModel.getFileNameWithoutExtension(), null);
		String method = codeStringGeneratorService.generateMethod(acessLevel, isStatic, returnType, methodName, arguments);
	int endLine = getCorrectLine(codeGeneratorModel.getMethodEndLine());
		javaService.insertLine(file, method, endLine);
	}

	@Override
	public void generateMethod(AcessLevel acessLevel, boolean isStatic, String returnType, String methodName,
			List<Field> arguments, String returnValue) {
		File file = codeGeneratorModel.getOpenedFile();
		codeGeneratorModel.parse(file, codeGeneratorModel.getFileNameWithoutExtension(), null);
		String method = codeStringGeneratorService.generateMethod(acessLevel, isStatic, returnType, methodName, arguments, returnValue);
			int endLine = getCorrectLine(codeGeneratorModel.getMethodEndLine());
		javaService.insertLine(file, method, endLine);
	}

	@Override
	public void generateMethod(AcessLevel acessLevel, boolean isStatic, String returnType, String methodName,
			List<Field> arguments, String returnValue, String body) {
		File file = codeGeneratorModel.getOpenedFile();
		codeGeneratorModel.parse(file, codeGeneratorModel.getFileNameWithoutExtension(), null);
		String method = codeStringGeneratorService.generateMethod(acessLevel, isStatic, returnType, methodName, arguments, returnValue, body);
		int endLine = getCorrectLine(codeGeneratorModel.getMethodEndLine());
		javaService.insertLine(file, method, endLine);
	}

	@Override
	public void generateMethod(AcessLevel acessLevel, boolean isStatic, String returnType, String methodName,
			List<Field> arguments, int line) {
		File file = codeGeneratorModel.getOpenedFile();
		String method = codeStringGeneratorService.generateMethod(acessLevel, isStatic, returnType, methodName, arguments);
		javaService.insertLine(file, method, line);
	}

	@Override
	public void generateMethod(AcessLevel acessLevel, boolean isStatic, String returnType, String methodName,
			List<Field> arguments, String returnValue, int line) {
		File file = codeGeneratorModel.getOpenedFile();
		String method = codeStringGeneratorService.generateMethod(acessLevel, isStatic, returnType, methodName, arguments, returnValue);
		javaService.insertLine(file, method, line);
	}

	@Override
	public void generateMethod(AcessLevel acessLevel, boolean isStatic, String returnType, String methodName,
			List<Field> arguments, String returnValue, String body, int line) {
		File file = codeGeneratorModel.getOpenedFile();
		String method = codeStringGeneratorService.generateMethod(acessLevel, isStatic, returnType, methodName, arguments, returnValue, body);
		javaService.insertLine(file, method, line);
	}

	//Gets end of file line if there's no method
	private int getCorrectLine(int methodEndLine) {
		int endLine = methodEndLine <= 0 ? codeGeneratorModel.getEndOfFileLine() - 1 : methodEndLine + 2;
		return endLine;
	}

	//Inserts a string after the last field or after the class initial line if there's no fields
	private void insertAfterField(File file, String constructor) {
		int fieldEndLine = codeGeneratorModel.getFieldEndLine();
		int endLine = fieldEndLine <= 0 ? codeGeneratorModel.getClassInitLine() : fieldEndLine;
		if(endLine <= 0) {
			javaService.insertLine(file, constructor, endLine);	
		}
	}
}
