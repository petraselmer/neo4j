/**
 * Copyright (c) 2002-2014 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher

import org.hamcrest.CoreMatchers._
import org.junit.Assert._
import org.neo4j.cypher.internal.compiler.v2_2.commands.expressions.StringHelper

class ErrorMessagesTest extends ExecutionEngineFunSuite with StringHelper {

  test("fails when merging relationship with null property") {
    expectError("create (a) create (b) merge (a)-[r:X {p: null}]->(b) return r", "Cannot merge relationship using null property value for p")
  }

  test("fails when merging node with null property") {
    expectError("merge (n {x: null}) return n", "Cannot merge node using null property value for x")
  }

  test("noReturnColumns") {
    expectError(
      "match (s) where id(s) = 0 return",
      "Unexpected end of input: expected whitespace, DISTINCT, '*' or an expression (line 1, column 33)"
    )
  }

  test("badNodeIdentifier") {
    expectError(
      "match (a) where id(a) = 0 MATCH a-[WORKED_ON]-, return a",
      "Invalid input ',': expected whitespace, '>' or a node pattern (line 1, column 47)"
    )
  }

  test("badStart") {
    expectError(
      "starta = node(0) return a",
      "Invalid input 'a' (line 1, column 6)"
    )
  }

  test("functionDoesNotExist") {
    expectSyntaxError(
      "match (a) where id(a) = 0 return dontDoIt(a)",
      "Unknown function 'dontDoIt' (line 1, column 34)",
      33
    )
  }

  test("noIndexName") {
    expectSyntaxError(
      "start a = node(name=\"sebastian\") match a-[:WORKED_ON]-b return b",
      "Invalid input 'n': expected whitespace, an unsigned integer, a parameter or '*' (line 1, column 16)",
      15
    )
  }

  test("aggregateFunctionInWhere") {
    expectError(
      "match (a) where id(a) = 0 and count(a) > 10 RETURN a",
      "Invalid use of aggregating function count(...) in this context (line 1, column 31)"
    )
  }

  test("twoIndexQueriesInSameStart") {
    expectSyntaxError(
      "start a = node:node_auto_index(name=\"sebastian\",name=\"magnus\") return a",
      "Invalid input ',': expected whitespace or ')' (line 1, column 48)",
      47
    )
  }

  test("badMatch2") {
    expectSyntaxError(
      "match (p) where id(p) = 2 match p-[:IS_A]>dude return dude.name",
      "Invalid input '>': expected whitespace or '-' (line 1, column 42)",
      41
    )
  }

  test("badMatch3") {
    expectSyntaxError(
      "match (p) where id(p) = 2 match p-[:IS_A->dude return dude.name",
      "Invalid input '-': expected an identifier character, whitespace, '|', a length specification, a property map or ']' (line 1, column 41)",
      40
    )
  }

  test("badMatch4") {
    expectSyntaxError(
      "match (p) where id(p) = 2 match p-[!]->dude return dude.name",
      "Invalid input '!': expected whitespace, an identifier, '?', relationship types, a length specification, a property map or ']' (line 1, column 36)",
      35
    )
  }

  test("badMatch5") {
    expectSyntaxError(
      "match (p) where id(p) = 2 match p[:likes]->dude return dude.name",
      "Invalid input '[': expected an identifier character, whitespace, '=', node labels, a property map, " +
        "a relationship pattern, ',', USING, WHERE, LOAD CSV, START, MATCH, UNWIND, MERGE, CREATE, SET, DELETE, REMOVE, FOREACH, WITH, " +
        "RETURN, UNION, ';' or end of input (line 1, column 34)",
      33
    )
  }

  test("invalidLabel") {
    expectSyntaxError(
      "match (p) where id(p) = 2 match (p:super-man) return p.name",
      "Invalid input 'm': expected whitespace, [ or '-' (line 1, column 42)",
      41
    )
  }

  test("noEqualsSignInStart") {
    expectSyntaxError(
      "start r:relationship:rels() return r",
      "Invalid input ':': expected an identifier character, whitespace or '=' (line 1, column 8)",
      7
    )
  }

  test("relTypeInsteadOfRelIdInStart") {
    expectSyntaxError(
      "start r = relationship(:WORKED_ON) return r",
      "Invalid input ':': expected whitespace, an unsigned integer, a parameter or '*' (line 1, column 24)",
      23
    )
  }

  test("noNodeIdInStart") {
    expectSyntaxError(
      "start r = node() return r",
      "Invalid input ')': expected whitespace, an unsigned integer, a parameter or '*' (line 1, column 16)",
      15
    )
  }

  test("startExpressionWithoutIdentifier") {
    expectSyntaxError(
      "start a = node:node_auto_index(name=\"magnus\"),node:node_auto_index(name=\"sebastian) return b,c",
      "Invalid input ':': expected an identifier character, whitespace or '=' (line 1, column 51)",
      50
    )
  }

  test("functions and stuff have to be renamed when sent through with") {
    expectError(
      "match (a) where id(a) = 0 with a, count(*) return a",
      "Expression in WITH must be aliased (use AS) (line 1, column 35)"
    )
  }

  test("missing dependency correctly reported") {
    expectError(
      "match (a) where id(a) = 0 CREATE a-[:KNOWS]->(b {name:missing}) RETURN b",
      "missing not defined (line 1, column 55)"
    )
  }

  test("missing create dependency correctly reported") {
    expectError(
      "match (a) where id(a) = 0 CREATE a-[:KNOWS]->(b {name:missing}) RETURN b",
      "missing not defined (line 1, column 55)"
    )
  }

  test("missing set dependency correctly reported") {
    expectError(
      "match (a) where id(a) = 0 SET a.name = missing RETURN a",
      "missing not defined (line 1, column 40)"
    )
  }

  test("create with identifier already existing") {
    expectError(
      "match (a) where id(a) = 0 CREATE (a {name:'foo'}) RETURN a",
      "a already declared (line 1, column 35)"
    )
  }

  test("create with identifier already existing2") {
    expectError(
      "match (a) where id(a) = 0 CREATE UNIQUE (a {name:'foo'})-[:KNOWS]->() RETURN a",
      "Can't create `a` with properties or labels here. It already exists in this context"
    )
  }

  test("merge 2 nodes with same identifier but different labels") {
    expectError(
      "MERGE (a: Foo)-[r:KNOWS]->(a: Bar)",
      "Can't create `a` with properties or labels here. It already exists in this context"
    )
  }

  test("type of identifier is wrong") {
    expectError(
      "match (n) where id(n) = 0 with [n] as users MATCH users-->messages RETURN messages",
      "Type mismatch: users already defined with conflicting type Collection<Node> (expected Node) (line 1, column 51)"
    )
  }

  test("warn about exclamation mark") {
    expectError(
      "match (n) where id(n) = 0 and n.foo != 2 return n",
      "Unknown operation '!=' (you probably meant to use '<>', which is the operator for inequality testing) (line 1, column 37)"
    )
  }

  test("warn about type error") {
    expectError(
      "match (p) where id(p) = 0 MATCH p-[r*]->() WHERE r.foo = 'apa' RETURN r",
      "Type mismatch: expected Map, Node or Relationship but was Collection<Relationship> (line 1, column 50)"
    )
  }

  test("missing something to delete") {
    expectError(
      "match (p) where id(p) = 0 DELETE x",
      "x not defined (line 1, column 34)"
    )
  }

  test("unions must have the same columns") {
    expectError(
      """MATCH a WHERE id(a) = 0 RETURN a
         UNION
         MATCH b WHERE id(b) = 0 RETURN b""",
      "All sub queries in an UNION must have the same column names"
    )
  }

  test("can not mix union and union all") {
    expectError(
      """match (a) where id(a) = 0 RETURN a
         UNION
         match (a) where id(a) = 0 RETURN a
         UNION ALL
         match (a) where id(a) = 0 RETURN a""",
      "Invalid combination of UNION and UNION ALL (line 4, column 10)"
    )
  }

  test("can not use optional pattern as predicate") {
    expectError(
      "match (a) where id(a) = 1 RETURN a-[?]->()",
      "Optional relationships cannot be specified in this context (line 1, column 35)"
    )
  }

  test("trying to drop constraint index should return sensible error") {
    graph.createConstraint("LabelName", "Prop")

    expectError(
      "DROP INDEX ON :LabelName(Prop)",
      "Unable to drop index on :LabelName(Prop): Index belongs to constraint: :LabelName(Prop)"
    )
  }

  test("trying to drop non existent index") {
    expectError(
      "DROP INDEX ON :Person(name)",
      "Unable to drop index on :Person(name): No such INDEX ON :Person(name)."
    )
  }

  test("trying to add unique constraint when duplicates exist") {
    createLabeledNode(Map("name" -> "A"), "Person")
    createLabeledNode(Map("name" -> "A"), "Person")

    expectError(
      "CREATE CONSTRAINT ON (person:Person) ASSERT person.name IS UNIQUE",
      String.format("Unable to create CONSTRAINT ON ( person:Person ) ASSERT person.name IS UNIQUE:%n" +
        "Multiple nodes with label `Person` have property `name` = 'A':%n  node(0)%n  node(1)")
    )
  }

  test("drop a non existent constraint") {
    expectError(
      "DROP CONSTRAINT ON (person:Person) ASSERT person.name IS UNIQUE",
      "No such constraint"
    )
  }

  test("create without specifying direction should fail") {
    expectError(
      "CREATE (a)-[:FOO]-(b) RETURN a,b",
      "Only directed relationships are supported in CREATE"
    )
  }

  test("create without specifying direction should fail2") {
    expectError(
      "CREATE (a)<-[:FOO]->(b) RETURN a,b",
      "Only directed relationships are supported in CREATE"
    )
  }

  test("report deprecated use of property name with question mark") {
    expectError(
      "match (n) where id(n) = 0 return n.title? = \"foo\"",
      "This syntax is no longer supported (missing properties are now returned as null). Please use (not(has(<ident>.title)) OR <ident>.title=<value>) if you really need the old behavior."
    )
  }

  test("report deprecated use of property name with exclamation mark") {
    expectError(
      "match (n) where id(n) = 0 return n.title! = \"foo\"",
      "This syntax is no longer supported (missing properties are now returned as null)."
    )
  }

  test("recommend using remove when user tries to delete a label") {
    expectError(
      "match (n) where id(n) = 0 delete n:Person",
      "DELETE doesn't support removing labels from a node. Try REMOVE."
    )
  }

  test("report wrong usage of index hint") {
    graph.createConstraint("Person", "id")
    expectError(
      "MATCH (n:Person) USING INDEX n:Person(id) WHERE n.id = 12 OR n.id = 14 RETURN n",
      "Cannot use index hint in this context. Index hints require using a simple equality comparison or IN condition in WHERE (either directly or as part of a top-level AND). Note that the label and property comparison must be specified on a non-optional node"
    )
  }

  test("report wrong usage of label scan hint") {
    expectError(
      "MATCH (n) USING SCAN n:Person WHERE n:Person OR n:Bird RETURN n",
      "Cannot use label scan hint in this context. Label scan hints require using a simple label test in WHERE (either directly or as part of a top-level AND). Note that the label must be specified on a non-optional node")
  }

  test("should give nice error when setting a property to a illegal value") {
    expectError(
      "CREATE (a) SET a.foo = [{x: 1}]",
      "Property values can only be of primitive types or arrays thereof")
  }

  test("should forbid using same introduced relationship twice in one MATCH pattern") {
    expectError("match (a)-[r]->(b)-[r]-(c) return r", "Cannot use the same relationship identifier 'r' for multiple patterns (line 1, column 21)")
  }

  test("should not allow binding a path name that is already bound") {
    expectError(
      "match p = a with p,a match p = a-->b return a",
      "p already declared (line 1, column 28)\n\"match p = a with p,a match p = a-->b return a"
    )
  }

  test("should forbid using duplicate ids in return/with") {
    expectError(
      "return 1, 1",
      "Multiple result columns with the same name are not supported (line 1, column 8)"
    )
  }

  test("should forbid 'RETURN *' when there are no identifiers in scope") {
    expectError(
      "match () return *",
      "RETURN * is not allowed when there are no identifiers in scope"
    )
  }

  test("should forbid bound relationship list in shortestPath pattern parts") {
    expectError(
      "WITH [] AS r LIMIT 1 MATCH p = shortestPath(src-[r*]->dst) RETURN src, dst",
      "Bound relationships not allowed in shortestPath(...)"
    )
  }

  test("match (n) where id(n) = 1 is no longer supported") {
    expectError(
      "START n = node(1) RETURN n",
      "Using 'START n = node(1)' is no longer supported.  Please instead use 'MATCH n WHERE id(n) = 1"
    )
  }

  test("START n = node(1, 2) is no longer supported") {
    expectError(
      "START n = node(1, 2) RETURN n",
      "Using 'START n = node(1, 2)' is no longer supported.  Please instead use 'MATCH n WHERE id(n) IN [1, 2]"
    )
  }

  def expectError(query: String, expectedError: String) {
    val error = intercept[CypherException](executeQuery(query))
    assertThat(error.getMessage, containsString(expectedError))
  }

  private def expectSyntaxError(query: String, expectedError: String, expectedOffset: Int) {
    val error = intercept[SyntaxException](executeQuery(query))
    assertThat(error.getMessage(), containsString(expectedError))
    assertThat(error.offset, equalTo(Some(expectedOffset): Option[Int]))
  }

  def executeQuery(query: String) {
    execute(query).toList
  }
}
