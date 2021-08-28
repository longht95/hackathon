package sql.generator.hackathon.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.RowConstructor;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.util.TablesNamesFinder;
import sql.generator.hackathon.model.ColumnInfo;
import sql.generator.hackathon.model.Condition;
import sql.generator.hackathon.model.InfoDisplayScreen;
import sql.generator.hackathon.model.ParseObject;
import sql.generator.hackathon.model.TableSQL;

@Service
public class ServiceParse {
	private Map<String, TableSQL> tables;
	private Map<String, List<String>> parentAlias;
	private Map<String, List<String>> mappingKey;
	private static Map<String, String> reverseExpression = new HashMap<>();
	private List<Condition> listCondition;
	private Map<String,Set<ColumnInfo>> listColumnInfo;
	
	public static final String NOT_IN = "NOT IN";
	public static final String IN = "IN";
	public static final String GREATHER_OR_EQUAL = ">=";
	public static final String LESS_OR_EQUAL = "<=";
	public static final String GREATHER = ">";
	public static final String LESS = "<";
	public static final String EQUAL = "=";
	public static final String EQUAL_NOT = "!=";
	public static final String DOT = ".";
	public static final String LIKE = "LIKE";
	private int state = 1;
	
	private Map<String, Set<String>> listColumnAlias;

	static {
		reverseExpression.put("=", "!=");
		reverseExpression.put("!=", "=");
		reverseExpression.put("<", ">");
		reverseExpression.put(">", "<");
		reverseExpression.put(">=", "<");
		reverseExpression.put("<=", ">");
	}
	
	public Map<String, TableSQL> getColumnInfo(String query) throws JSQLParserException {
		Select select = (Select) CCJSqlParserUtil.parse(query);
		listColumnInfo = new HashMap<>();
		tables = new HashMap<>();
		parentAlias = new HashMap<>();
		listColumnAlias = new HashMap<>();
		SelectBody selectBody = select.getSelectBody();
		processSelectBody(selectBody, null);
		processColumnWithAlias();
		return tables;
	}
	
	public InfoDisplayScreen getColumnInfoView(String query, String tableName) throws JSQLParserException {
		Select select = (Select) CCJSqlParserUtil.parse(query);
		listColumnInfo = new HashMap<>();
		tables = new HashMap<>();
		parentAlias = new HashMap<>();
		listColumnAlias = new HashMap<>();
		SelectBody selectBody = select.getSelectBody();
		processSelectBody(selectBody, null);
		processColumnWithAlias();
		
		InfoDisplayScreen infoDisplayScreen = new InfoDisplayScreen();
		tables.entrySet().forEach(x -> {
			if (x.getValue().getTableName().equals(tableName)) {
				infoDisplayScreen.listColumnName = x.getValue().getColumns().stream().collect(Collectors.toList());
			}
		});
		return infoDisplayScreen;
	}
	
	private void processColumnWithAlias() {
		listColumnAlias.entrySet().forEach(x -> {
			TableSQL tbl = tables.get(x.getKey());
			Set<String> listTMP = new HashSet<>();
			if (tbl != null) {
				if (tbl.columns != null) {
					listTMP = tbl.columns;
				}
				listTMP.addAll(x.getValue());
				tbl.setColumns(listTMP);
				tables.put(x.getKey(), tbl);
			} else {
				List<String> alias = parentAlias.get(x.getKey());
				if (alias != null && !alias.isEmpty()) {
					for (String s : alias) {
						tbl = tables.get(s);
						if (tbl != null) {
							if (tbl.columns != null) {
								listTMP = tbl.columns;
							}
							for (String z : x.getValue()) {
								listTMP.add(z);
							}
							tbl.setColumns(listTMP);
							tables.put(s, tbl);
						}
						
					}
				}
			}
		});
	}
	
	private void processSelectBody(SelectBody selectBody, Alias alias) throws JSQLParserException {
		if (selectBody instanceof PlainSelect) {
			processSingle((PlainSelect) selectBody, alias);
		} else if (selectBody instanceof WithItem) {
		} else {
			SetOperationList operationList = (SetOperationList) selectBody;
			if (operationList.getSelects() != null && !operationList.getSelects().isEmpty()) {
				List<SelectBody> plainSelects = operationList.getSelects();
				for (SelectBody plainSelect : plainSelects) {
					processSelectBody(plainSelect, alias);
				}
			}
		}
	}

	
	private void processSingle(PlainSelect plainSelect, Alias alias) throws JSQLParserException {
		String tableNameORAlias;
		if (plainSelect.getFromItem() instanceof Table) {
			Table table = (Table) plainSelect.getFromItem();
			tableNameORAlias = table.getAlias() != null ? table.getAlias().getName() : table.getName();
		} else {
			if (plainSelect.getFromItem().getAlias() == null) {
				throw new JSQLParserException();
			}
			tableNameORAlias = plainSelect.getFromItem().getAlias().getName();
		}
		List<SelectItem> selectItems = plainSelect.getSelectItems();
		if (selectItems != null && !selectItems.isEmpty()) {
			listColumnAlias.put(tableNameORAlias.replace(" ", ""),selectItems.stream().map(x -> x.toString()).collect(Collectors.toSet()));
		}
		
		
		
		processFrom(plainSelect.getFromItem(), alias);
		List<Join> joins = plainSelect.getJoins();
		if (joins != null && !joins.isEmpty()) {
			joins.forEach(join -> {
				try {
					processJoinColumn(join, plainSelect.getFromItem(), selectItems);
				} catch (JSQLParserException e) {
					e.printStackTrace();
				}
			});
		}
		if (plainSelect.getWhere() != null) {
			processExpression(plainSelect.getWhere(), plainSelect.getFromItem());
		}

	}

	private void processJoinColumn(Join join, FromItem fromItem, List<SelectItem> selectItems) throws JSQLParserException {

		if (join.getRightItem() instanceof SubSelect) {
			SubSelect subSelect = (SubSelect) join.getRightItem();
			processExpression(join.getOnExpression(), fromItem);
			processSelectBody(subSelect.getSelectBody(), null);

		} else {
			processExpression(join.getOnExpression(), fromItem);
			processTable(join.getRightItem());
			String tableNameORAlias = join.getRightItem().getAlias() != null ? join.getRightItem().getAlias().getName() : join.getRightItem().toString();
			if (selectItems != null && !selectItems.isEmpty()) {
				listColumnAlias.put(tableNameORAlias.replace(" ", ""),selectItems.stream().map(x -> x.toString()).collect(Collectors.toSet()));
			}
		}
	}
	
	private void processExpression(Expression expression, FromItem fromItem) {
		if (expression != null) {
			expression.accept(new ExpressionVisitorAdapter() {
				@Override
				protected void visitBinaryExpression(BinaryExpression expr) {
					if (expr.getLeftExpression() instanceof Column) {

						Column column = (Column) expr.getLeftExpression();
						String aliasOrTable = column.getTable() == null
								? fromItem.getAlias() != null ? fromItem.getAlias().getName() : fromItem.toString()
								: column.getTable().getAlias() != null ? column.getTable().getAlias().getName()
										: column.getTable().getName();
						Set<String> listTMP = listColumnAlias.get(aliasOrTable);
						if (listTMP == null) {
							listTMP = new HashSet<>();
						}
						listTMP.add(column.getColumnName());
						listColumnAlias.put(aliasOrTable.replace(" ", ""), listTMP);
					}
					if (expr.getRightExpression() instanceof Column) {
						Column column = (Column) expr.getRightExpression();
						String aliasOrTable = column.getTable() == null
								? fromItem.getAlias() != null ? fromItem.getAlias().getName() : fromItem.toString()
								: column.getTable().getAlias() != null ? column.getTable().getAlias().getName()
										: column.getTable().getName();
						Set<String> listTMP = listColumnAlias.get(aliasOrTable);
						if (listTMP == null) {
							listTMP = new HashSet<>();
						}
						listTMP.add(column.getColumnName());
						listColumnAlias.put(aliasOrTable.replace(" ", ""), listTMP);
					}
					super.visitBinaryExpression(expr);
				}
			});
		}
		

	}

	private void processFrom(FromItem fromItem, Alias alias) throws JSQLParserException {
		if (fromItem instanceof SubSelect) {
			SubSelect subSelect = (SubSelect) fromItem;
			processSelectBody(subSelect.getSelectBody(), alias != null ? alias : fromItem.getAlias());
		} else {
			processTable(fromItem);
			if (alias != null) {
				processAlias(alias, fromItem);
			}
		}
	}
	
	private void processTableColumn(FromItem fromItem) {
		Table table = (Table) fromItem;
		String alias = table.getAlias() != null ? table.getAlias().getName() : table.getName();
		Set<ColumnInfo> columnInfo = listColumnInfo.get(alias);
		if (columnInfo == null) {
			columnInfo = new HashSet<>();
		}
		listColumnInfo.put(alias, columnInfo);
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	public List<String> dataToSqlInsert(Map<String, List<List<ColumnInfo>>> listData) {

		List<String> listSQL = new ArrayList<>();
		for (java.util.Map.Entry<String, List<List<ColumnInfo>>> s : listData.entrySet()) {
			s.getValue().forEach(x -> {
				Insert insert = new Insert();
				Table table = new Table(s.getKey().toString());
				List<Column> columnList = new ArrayList<>();
				ExpressionList values = new ExpressionList();
				for (ColumnInfo columnInfo : x) {
					columnList.add(new Column(columnInfo.name));
					values.addExpressions(new StringValue(columnInfo.val));
				}
				insert.setTable(table);
				insert.setItemsList(values);
				insert.setColumns(columnList);
				listSQL.add(insert.toString());
			});

		}

		return listSQL;
	}

	public ParseObject parseSelectStatement(String query) throws JSQLParserException {
		tables = new HashMap<>();
		parentAlias = new HashMap<>();
		listCondition = new ArrayList<>();
		mappingKey = new HashMap<>();
		state = 1;
		Select select = (Select) CCJSqlParserUtil.parse(query);
		processSelectBody(select.getSelectBody(), false, null);
		processPushCondition();
		ParseObject parseObject = new ParseObject();
		parseObject.listTableSQL = tables.entrySet().stream().map(table -> table.getValue())
				.collect(Collectors.toList());
		parseObject.mappingKey = mappingKey;
		System.out.println(listCondition.toString());
		return parseObject;
	}

	public List<String> getListTableByStatement(String query) throws JSQLParserException {
		Statement statement = CCJSqlParserUtil.parse(query);
		Select selectStatement = (Select) statement;
		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
		return tablesNamesFinder.getTableList(selectStatement);
	}

	private void processPushCondition() {
		listCondition.stream().forEach(condition -> {
			String alias = condition.left.split("\\.")[0];
			TableSQL tableSQL = tables.get(alias);
			if (tableSQL == null) {
				List<String> childAlias = parentAlias.get(alias);
				childAlias.forEach(child -> {
					TableSQL childTable = tables.get(child);
					if (childTable.getCondition() == null) {
						condition.setLeft(condition.left.replace(alias, childTable.alias));
						childTable.setCondition(new ArrayList<>(Arrays.asList(condition)));
					} else {
						condition.setLeft(condition.left.replace(alias, childTable.alias));
						childTable.condition.add(condition);
					}
					tables.put(child, childTable);
				});
			} else {
				if (tableSQL.getCondition() == null) {

					tableSQL.setCondition(new ArrayList<>(Arrays.asList(condition)));
				} else {
					tableSQL.condition.add(condition);
				}
				tables.put(alias, tableSQL);
			}

		});
	}

	private void processSelectBody(SelectBody selectBody, boolean isNot, Alias alias) throws JSQLParserException {
		if (selectBody instanceof PlainSelect) {
			processSingle((PlainSelect) selectBody, isNot, alias);
		} else if (selectBody instanceof WithItem) {
		} else {
			SetOperationList operationList = (SetOperationList) selectBody;
			if (operationList.getSelects() != null && !operationList.getSelects().isEmpty()) {
				List<SelectBody> plainSelects = operationList.getSelects();
				for (SelectBody plainSelect : plainSelects) {
					processSelectBody(plainSelect, isNot, alias);
				}
			}
		}
	}

	private void processSingle(PlainSelect plainSelect, boolean isNot, Alias alias) throws JSQLParserException {
		processFromItem(plainSelect.getFromItem(), alias);
		List<Join> joins = plainSelect.getJoins();
		if (joins != null && !joins.isEmpty()) {
			joins.forEach(join -> {
				try {
					processJoin(join);
				} catch (JSQLParserException e) {
					e.printStackTrace();
				}
			});
		}
		if (plainSelect.getWhere() != null) {

			processExpression(plainSelect.getWhere(), isNot, plainSelect.getFromItem());
		}

	}

	private void processJoin(Join join) throws JSQLParserException {

		if (join.getRightItem() instanceof SubSelect) {
			SubSelect subSelect = (SubSelect) join.getRightItem();
			processExpression(join.getOnExpression(), false, join.getRightItem());
			processSelectBody(subSelect.getSelectBody(), false, join.getRightItem().getAlias());

		} else {
			processExpression(join.getOnExpression(), false, join.getRightItem());
			processTable(join.getRightItem());
		}
	}

	private void processAlias(Alias parent, FromItem current) {
		List<String> listAlias = parentAlias.get(parent.getName());
		if (listAlias == null) {
			listAlias = new ArrayList<>();
		}
		String currentAlias = current.getAlias() != null ? current.getAlias().getName() : current.toString();
		listAlias.add(currentAlias);
		parentAlias.put(parent.getName(), listAlias);
	}

	private void processTable(FromItem fromItem) {
		Table table = (Table) fromItem;
		String alias = table.getAlias() != null ? table.getAlias().getName() : table.getName();
		TableSQL tbl = tables.get(alias);
		if (tbl == null) {
			tbl = TableSQL.builder().tableName(table.getName()).alias(alias).condition(new ArrayList<>()).build();
		}
		tables.put(tbl.getAlias(), tbl);
	}

	private void processFromItem(FromItem fromItem, Alias alias) throws JSQLParserException {
		if (fromItem instanceof SubSelect) {
			SubSelect subSelect = (SubSelect) fromItem;
			processSelectBody(subSelect.getSelectBody(), false, alias != null ? alias : fromItem.getAlias());
		} else {
			processTable(fromItem);
			if (alias != null) {
				processAlias(alias, fromItem);
			}
		}
	}

	private void processExpression(Expression expression, boolean isNot, FromItem alias) throws JSQLParserException {
		String currentAlias;
		if (alias instanceof Table) {
			currentAlias = alias.getAlias() != null ? alias.getAlias().getName() : alias.toString();
		} else {
			currentAlias = alias.getAlias().getName();
		}
		if (expression instanceof EqualsTo || expression instanceof GreaterThan
				|| expression instanceof GreaterThanEquals || expression instanceof MinorThan
				|| expression instanceof MinorThanEquals || expression instanceof NotEqualsTo) {
			BinaryExpression binary = (BinaryExpression) expression;
			if (binary.getLeftExpression() instanceof Column && binary.getRightExpression() instanceof Column) {
				String expresionLeft;
				String expresionRight;
				if (binary.getStringExpression().equals(GREATHER_OR_EQUAL)
						|| binary.getStringExpression().equals(LESS_OR_EQUAL)
						|| binary.getStringExpression().equals(GREATHER) || binary.getStringExpression().equals(LESS)) {
					expresionLeft = isNot ? reverseExpression.get(binary.getStringExpression())
							: binary.getStringExpression();
					expresionRight = isNot ? binary.getStringExpression()
							: reverseExpression.get(binary.getStringExpression());
				} else {
					expresionRight = isNot ? reverseExpression.get(binary.getStringExpression())
							: binary.getStringExpression();
					expresionLeft = isNot ? reverseExpression.get(binary.getStringExpression())
							: binary.getStringExpression();

				}
				Column leftColumn = (Column) binary.getLeftExpression();
				Column rightColumn = (Column) binary.getRightExpression();
				if (leftColumn.getTable() == null) {
					leftColumn.setTable(new Table(currentAlias));
				}
				if (rightColumn.getTable() == null) {
					rightColumn.setTable(new Table(currentAlias));
				}
				Condition conditionLeft = Condition.builder().left(leftColumn.toString()).expression(expresionLeft)
						.right("KEY" + state).build();
				listCondition.add(conditionLeft);
				Condition conditionRight = Condition.builder().left(rightColumn.toString()).expression(expresionRight)
						.right("KEY" + state).build();

				mappingKey.put("KEY" + state,
						new ArrayList<>(Arrays.asList(leftColumn.toString(), rightColumn.toString())));
				state++;
				listCondition.add(conditionRight);
			} else if (binary.getRightExpression() instanceof Column) {
				Condition condition = Condition.builder().left(binary.getRightExpression().toString())
						.expression(binary.getStringExpression()).right(binary.getLeftExpression().toString()).build();
				listCondition.add(condition);
			} else if (binary.getLeftExpression() instanceof Column
					&& binary.getRightExpression() instanceof SubSelect) {
				
				Column leftColumn = (Column) binary.getLeftExpression();
				if (leftColumn.getTable() == null) {
					leftColumn.setTable(new Table(currentAlias));
				}
				SelectBody selectBody = ((SubSelect) binary.getRightExpression()).getSelectBody();
				PlainSelect plainSelect = (PlainSelect) selectBody;
				List<String> listItems = new ArrayList<>();
				Table table = (Table) plainSelect.getFromItem();
				List<SelectItem> selectItems = plainSelect.getSelectItems();
				listItems.addAll(selectItems.stream().map(item -> {
					if (item.toString().contains(DOT)) {
						return item.toString();
					} else {
						return table.getAlias() != null ? table.getAlias().getName() + "." + item.toString()
								: table.getName() + "." + item.toString();
					}
				}).collect(Collectors.toList()));
				Condition condition = Condition.builder().left(leftColumn.toString())
						.expression(binary.getStringExpression()).right("KEY"+state).build();
				Condition conditionRight = Condition.builder().left(selectItems.get(0).toString())
						.expression(binary.getStringExpression()).right("KEY"+state).build();
				listCondition.add(condition);
				listCondition.add(conditionRight);
				mappingKey.put("KEY" + state,
						new ArrayList<>(Arrays.asList(leftColumn.toString(), selectItems.get(0).toString())));
				state++;
				processSelectBody(selectBody, isNot, null);
			} else {
				Column leftColumn = (Column) binary.getLeftExpression();
				if (leftColumn.getTable() == null) {
					leftColumn.setTable(new Table(currentAlias));
				}
				Condition condition = Condition.builder().left(leftColumn.toString())
						.expression(binary.getStringExpression()).right(binary.getRightExpression().toString()).build();
				listCondition.add(condition);
			}
		} else if (expression instanceof AndExpression) {
			AndExpression andExpression = (AndExpression) expression;
			processExpression(andExpression.getLeftExpression(), isNot, alias);
			processExpression(andExpression.getRightExpression(), isNot, alias);
		} else if (expression instanceof OrExpression) {
			OrExpression orExpression = (OrExpression) expression;
			processExpression(orExpression.getLeftExpression(), isNot, alias);
		} else if (expression instanceof NotExpression) {
			NotExpression notExpression = (NotExpression) expression;
			processExpression(notExpression.getExpression(), true, alias);
		} else if (expression instanceof InExpression) {
			InExpression inExpression = (InExpression) expression;
			if (inExpression.getLeftExpression() instanceof RowConstructor) {
				RowConstructor row = (RowConstructor) inExpression.getLeftExpression();
				row.getExprList().getExpressions().forEach(r -> {
					if (r instanceof Column) {

					}
				});
			} else if (inExpression.getLeftExpression() instanceof Column) {
				Column leftColumn = (Column) inExpression.getLeftExpression();
				if (leftColumn.getTable() == null) {
					leftColumn.setTable(new Table(currentAlias));
				}
				if (inExpression.getRightItemsList() instanceof SubSelect) {
					SubSelect subSelect = (SubSelect) inExpression.getRightItemsList();
					SelectBody selectBody = subSelect.getSelectBody();
					if (selectBody instanceof PlainSelect) {
						PlainSelect plainSelect = (PlainSelect) selectBody;
						List<String> listItems = new ArrayList<>();
						Table table = (Table) plainSelect.getFromItem();
						List<SelectItem> selectItems = plainSelect.getSelectItems();
						listItems.addAll(selectItems.stream().map(item -> {
							if (item.toString().contains(DOT)) {
								return item.toString();
							} else {
								return table.getAlias() != null ? table.getAlias().getName() + "." + item.toString()
										: table.getName() + "." + item.toString();
							}
						}).collect(Collectors.toList()));
						Condition conditionLeft = Condition.builder().left(leftColumn.toString()).right("KEY" + state)
								.expression(inExpression.isNot() ? EQUAL_NOT : EQUAL).build();

						listCondition.add(conditionLeft);
						Condition conditionRight = Condition.builder().left(listItems.get(0)).right("KEY" + state)
								.expression(inExpression.isNot() ? EQUAL_NOT : EQUAL).build();
						listCondition.add(conditionRight);

						mappingKey.put("KEY" + state,
								new ArrayList<>(Arrays.asList(leftColumn.toString(), listItems.get(0))));
						state++;
					} else {
						// operation subquery ... UNION
						SetOperationList operationList = (SetOperationList) selectBody;
						List<String> listItems = new ArrayList<>();
						operationList.getSelects().forEach(o -> {
							PlainSelect plainSelect = (PlainSelect) o;
							Table table = (Table) plainSelect.getFromItem();
							listItems.addAll(plainSelect.getSelectItems().stream().map(item -> {
								if (item.toString().contains(DOT)) {
									return item.toString();
								} else {
									return table.getName() + "." + item.toString();
								}
							}).collect(Collectors.toList()));
						});
						Condition condition = Condition.builder().left(leftColumn.toString())
								.expression(inExpression.isNot() ? NOT_IN : IN).listRight(listItems).build();
						listCondition.add(condition);
					}
					processSelectBody(selectBody, false, null);
				} else if (inExpression.getRightItemsList() instanceof ExpressionList) {
					ExpressionList expressionList = (ExpressionList) inExpression.getRightItemsList();
					Condition condition = Condition.builder().left(leftColumn.toString())
							.expression(inExpression.isNot() ? NOT_IN : IN).listRight(expressionList.getExpressions()
									.stream().map(value -> value.toString()).collect(Collectors.toList()))
							.build();
					listCondition.add(condition);
				}
			}

		} else if (expression instanceof ExistsExpression) {
			ExistsExpression existsExpression = (ExistsExpression) expression;
			if (existsExpression.getRightExpression() instanceof SubSelect) {
				SubSelect subSelect = (SubSelect) existsExpression.getRightExpression();
				processSelectBody(subSelect.getSelectBody(), isNot, null);
			}
		} else if (expression instanceof Parenthesis) {
			Parenthesis parenthesis = (Parenthesis) expression;
			processExpression(parenthesis.getExpression(), isNot, alias);
		} else if (expression instanceof LikeExpression) {
			LikeExpression likeExpression = (LikeExpression) expression;
			if (likeExpression.getLeftExpression() instanceof Column
					&& likeExpression.getRightExpression() instanceof StringValue) {
				Column leftColumn = (Column) likeExpression.getLeftExpression();
				if (leftColumn.getTable() == null) {
					leftColumn.setTable(new Table(currentAlias));
				}
				Condition condition = Condition.builder().left(leftColumn.toString())
						.expression(likeExpression.isNot() ? "NOT LIKE" : "LIKE").right(likeExpression.getRightExpression().toString()).build();
				listCondition.add(condition);
			}
		} else if (expression instanceof Between) {
			Between between = (Between) expression;
			Expression expressionStart = between.getBetweenExpressionStart();
			Expression expressionEnd = between.getBetweenExpressionEnd();
			Expression expressionLeft = between.getLeftExpression();
			Column leftColumn = (Column) expressionLeft;
			if (leftColumn.getTable() == null) {
				leftColumn.setTable(new Table(currentAlias));
			}
			if (expressionStart instanceof Function) {
				Function leftFunction = (Function) expressionStart;
				Condition condition = Condition.builder().left(leftColumn.toString())
						.right(leftFunction.getParameters().getExpressions().get(0).toString())
						.expression(isNot ? reverseExpression.get(GREATHER_OR_EQUAL) : GREATHER_OR_EQUAL)
						.function(leftFunction.getName()).build();
				listCondition.add(condition);
			} else if (expressionStart instanceof SubSelect) {
				SubSelect subSelect = (SubSelect) expressionStart;
				processSelectBody(subSelect.getSelectBody(), false, null);
				List<String> listItems = new ArrayList<>();
				List<SelectItem> selectItems = ((PlainSelect) subSelect.getSelectBody()).getSelectItems();

				listItems.addAll(selectItems.stream().map(item -> {
					return item.toString();
				}).collect(Collectors.toList()));

				Condition condition = Condition.builder().left(expressionLeft.toString())
						.expression(isNot ? reverseExpression.get(GREATHER_OR_EQUAL) : GREATHER_OR_EQUAL)
						.right("KEY" + state).build();

				String aliasRight = ((PlainSelect) subSelect.getSelectBody()).getFromItem().getAlias() != null
						? ((PlainSelect) subSelect.getSelectBody()).getFromItem().getAlias().getName()
						: ((PlainSelect) subSelect.getSelectBody()).getFromItem().toString();

				Condition conditionRight = Condition.builder().left(aliasRight + DOT + listItems.get(0))
						.expression(isNot ? reverseExpression.get(LESS_OR_EQUAL) : LESS_OR_EQUAL).right("KEY" + state)
						.build();
				mappingKey.put("KEY" + state,
						new ArrayList<>(Arrays.asList(expressionLeft.toString(), aliasRight + DOT + listItems.get(0))));
				listCondition.add(conditionRight);
				listCondition.add(condition);
				state++;
			} else {

				Condition condition = Condition.builder().left(between.getLeftExpression().toString())
						.expression(isNot ? reverseExpression.get(GREATHER_OR_EQUAL) : GREATHER_OR_EQUAL)
						.right(expressionStart.toString()).build();
				listCondition.add(condition);
			}

			if (expressionEnd instanceof Function) {
				Function functionRight = (Function) expressionEnd;
				Condition condition = Condition.builder().left(expressionLeft.toString())
						.right(functionRight.getParameters().getExpressions().get(0).toString())
						.expression(isNot ? reverseExpression.get(LESS_OR_EQUAL) : LESS_OR_EQUAL)
						.function(functionRight.getName()).build();
				listCondition.add(condition);
			} else if (expressionEnd instanceof SubSelect) {
				SubSelect subSelect = (SubSelect) expressionEnd;
				// is item select
				processSelectBody(subSelect.getSelectBody(), false, null);
				List<String> listItems = new ArrayList<>();
				List<SelectItem> selectItems = ((PlainSelect) subSelect.getSelectBody()).getSelectItems();
				listItems.addAll(selectItems.stream().map(item -> item.toString()).collect(Collectors.toList()));
				Condition condition = Condition.builder().left(expressionLeft.toString())
						.expression(isNot ? reverseExpression.get(LESS_OR_EQUAL) : LESS_OR_EQUAL).right("KEY" + state)
						.build();
				listCondition.add(condition);

				String aliasRight = ((PlainSelect) subSelect.getSelectBody()).getFromItem().getAlias() != null
						? ((PlainSelect) subSelect.getSelectBody()).getFromItem().getAlias().getName()
						: ((PlainSelect) subSelect.getSelectBody()).getFromItem().toString();

				Condition conditionRight = Condition.builder().left(aliasRight + DOT + listItems.get(0))
						.expression(isNot ? reverseExpression.get(GREATHER_OR_EQUAL) : GREATHER_OR_EQUAL)
						.right("KEY" + state).build();

				mappingKey.put("KEY" + state,
						new ArrayList<>(Arrays.asList(expressionLeft.toString(), aliasRight + DOT + listItems.get(0))));
				listCondition.add(conditionRight);
				state++;
			} else {
				Condition condition = Condition.builder().left(between.getLeftExpression().toString())
						.expression(isNot ? reverseExpression.get(LESS_OR_EQUAL) : LESS_OR_EQUAL)
						.right(expressionEnd.toString()).build();
				listCondition.add(condition);
			}
		}
	}
}
