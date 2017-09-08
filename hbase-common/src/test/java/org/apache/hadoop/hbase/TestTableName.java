begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
package|;
end_package

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertArrayEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertSame
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|testclassification
operator|.
name|MiscTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|testclassification
operator|.
name|MediumTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|TableName
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Bytes
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|TestWatcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|Description
import|;
end_import

begin_comment
comment|/**  * Returns a {@code byte[]} containing the name of the currently running test method.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestTableName
extends|extends
name|TestWatcher
block|{
specifier|private
name|TableName
name|tableName
decl_stmt|;
comment|/**    * Invoked when a test is about to start    */
annotation|@
name|Override
specifier|protected
name|void
name|starting
parameter_list|(
name|Description
name|description
parameter_list|)
block|{
name|tableName
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|description
operator|.
name|getMethodName
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|TableName
name|getTableName
parameter_list|()
block|{
return|return
name|tableName
return|;
block|}
name|String
name|emptyNames
index|[]
init|=
block|{
literal|""
block|,
literal|" "
block|}
decl_stmt|;
name|String
name|invalidNamespace
index|[]
init|=
block|{
literal|":a"
block|,
literal|"%:a"
block|}
decl_stmt|;
name|String
name|legalTableNames
index|[]
init|=
block|{
literal|"foo"
block|,
literal|"with-dash_under.dot"
block|,
literal|"_under_start_ok"
block|,
literal|"with-dash.with_underscore"
block|,
literal|"02-01-2012.my_table_01-02"
block|,
literal|"xyz._mytable_"
block|,
literal|"9_9_0.table_02"
block|,
literal|"dot1.dot2.table"
block|,
literal|"new.-mytable"
block|,
literal|"with-dash.with.dot"
block|,
literal|"legal..t2"
block|,
literal|"legal..legal.t2"
block|,
literal|"trailingdots.."
block|,
literal|"trailing.dots..."
block|,
literal|"ns:mytable"
block|,
literal|"ns:_mytable_"
block|,
literal|"ns:my_table_01-02"
block|}
decl_stmt|;
name|String
name|illegalTableNames
index|[]
init|=
block|{
literal|".dot_start_illegal"
block|,
literal|"-dash_start_illegal"
block|,
literal|"spaces not ok"
block|,
literal|"-dash-.start_illegal"
block|,
literal|"new.table with space"
block|,
literal|"01 .table"
block|,
literal|"ns:-illegaldash"
block|,
literal|"new:.illegaldot"
block|,
literal|"new:illegalcolon1:"
block|,
literal|"new:illegalcolon1:2"
block|}
decl_stmt|;
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IllegalArgumentException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testInvalidNamespace
parameter_list|()
block|{
for|for
control|(
name|String
name|tn
range|:
name|invalidNamespace
control|)
block|{
name|TableName
operator|.
name|isLegalFullyQualifiedTableName
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tn
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"invalid namespace "
operator|+
name|tn
operator|+
literal|" should have failed with IllegalArgumentException for namespace"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IllegalArgumentException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testEmptyNamespaceName
parameter_list|()
block|{
for|for
control|(
name|String
name|nn
range|:
name|emptyNames
control|)
block|{
name|TableName
operator|.
name|isLegalNamespaceName
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|nn
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"invalid Namespace name "
operator|+
name|nn
operator|+
literal|" should have failed with IllegalArgumentException"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IllegalArgumentException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testEmptyTableName
parameter_list|()
block|{
for|for
control|(
name|String
name|tn
range|:
name|emptyNames
control|)
block|{
name|TableName
operator|.
name|isLegalFullyQualifiedTableName
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tn
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"invalid tablename "
operator|+
name|tn
operator|+
literal|" should have failed with IllegalArgumentException"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLegalHTableNames
parameter_list|()
block|{
for|for
control|(
name|String
name|tn
range|:
name|legalTableNames
control|)
block|{
name|TableName
operator|.
name|isLegalFullyQualifiedTableName
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tn
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIllegalHTableNames
parameter_list|()
block|{
for|for
control|(
name|String
name|tn
range|:
name|illegalTableNames
control|)
block|{
try|try
block|{
name|TableName
operator|.
name|isLegalFullyQualifiedTableName
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tn
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"invalid tablename "
operator|+
name|tn
operator|+
literal|" should have failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// expected
block|}
block|}
block|}
class|class
name|Names
block|{
name|String
name|ns
decl_stmt|;
name|byte
index|[]
name|nsb
decl_stmt|;
name|String
name|tn
decl_stmt|;
name|byte
index|[]
name|tnb
decl_stmt|;
name|String
name|nn
decl_stmt|;
name|byte
index|[]
name|nnb
decl_stmt|;
name|Names
parameter_list|(
name|String
name|ns
parameter_list|,
name|String
name|tn
parameter_list|)
block|{
name|this
operator|.
name|ns
operator|=
name|ns
expr_stmt|;
name|nsb
operator|=
name|ns
operator|.
name|getBytes
argument_list|()
expr_stmt|;
name|this
operator|.
name|tn
operator|=
name|tn
expr_stmt|;
name|tnb
operator|=
name|tn
operator|.
name|getBytes
argument_list|()
expr_stmt|;
name|nn
operator|=
name|this
operator|.
name|ns
operator|+
literal|":"
operator|+
name|this
operator|.
name|tn
expr_stmt|;
name|nnb
operator|=
name|nn
operator|.
name|getBytes
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|Names
name|names
init|=
operator|(
name|Names
operator|)
name|o
decl_stmt|;
if|if
condition|(
operator|!
name|ns
operator|.
name|equals
argument_list|(
name|names
operator|.
name|ns
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|tn
operator|.
name|equals
argument_list|(
name|names
operator|.
name|tn
argument_list|)
condition|)
return|return
literal|false
return|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|ns
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|tn
operator|.
name|hashCode
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
name|Names
index|[]
name|names
init|=
operator|new
name|Names
index|[]
block|{
operator|new
name|Names
argument_list|(
literal|"n1"
argument_list|,
literal|"n1"
argument_list|)
block|,
operator|new
name|Names
argument_list|(
literal|"n2"
argument_list|,
literal|"n2"
argument_list|)
block|,
operator|new
name|Names
argument_list|(
literal|"table1"
argument_list|,
literal|"table1"
argument_list|)
block|,
operator|new
name|Names
argument_list|(
literal|"table2"
argument_list|,
literal|"table2"
argument_list|)
block|,
operator|new
name|Names
argument_list|(
literal|"table2"
argument_list|,
literal|"table1"
argument_list|)
block|,
operator|new
name|Names
argument_list|(
literal|"table1"
argument_list|,
literal|"table2"
argument_list|)
block|,
operator|new
name|Names
argument_list|(
literal|"n1"
argument_list|,
literal|"table1"
argument_list|)
block|,
operator|new
name|Names
argument_list|(
literal|"n1"
argument_list|,
literal|"table1"
argument_list|)
block|,
operator|new
name|Names
argument_list|(
literal|"n2"
argument_list|,
literal|"table2"
argument_list|)
block|,
operator|new
name|Names
argument_list|(
literal|"n2"
argument_list|,
literal|"table2"
argument_list|)
block|}
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testValueOf
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|TableName
argument_list|>
name|inCache
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|// fill cache
for|for
control|(
name|Names
name|name
range|:
name|names
control|)
block|{
name|inCache
operator|.
name|put
argument_list|(
name|name
operator|.
name|nn
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|ns
argument_list|,
name|name
operator|.
name|tn
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Names
name|name
range|:
name|names
control|)
block|{
name|assertSame
argument_list|(
name|inCache
operator|.
name|get
argument_list|(
name|name
operator|.
name|nn
argument_list|)
argument_list|,
name|validateNames
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|ns
argument_list|,
name|name
operator|.
name|tn
argument_list|)
argument_list|,
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|inCache
operator|.
name|get
argument_list|(
name|name
operator|.
name|nn
argument_list|)
argument_list|,
name|validateNames
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|nsb
argument_list|,
name|name
operator|.
name|tnb
argument_list|)
argument_list|,
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|inCache
operator|.
name|get
argument_list|(
name|name
operator|.
name|nn
argument_list|)
argument_list|,
name|validateNames
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|nn
argument_list|)
argument_list|,
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|inCache
operator|.
name|get
argument_list|(
name|name
operator|.
name|nn
argument_list|)
argument_list|,
name|validateNames
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|nnb
argument_list|)
argument_list|,
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|inCache
operator|.
name|get
argument_list|(
name|name
operator|.
name|nn
argument_list|)
argument_list|,
name|validateNames
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|name
operator|.
name|nsb
argument_list|)
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|name
operator|.
name|tnb
argument_list|)
argument_list|)
argument_list|,
name|name
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|TableName
name|validateNames
parameter_list|(
name|TableName
name|expected
parameter_list|,
name|Names
name|names
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|expected
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|names
operator|.
name|nn
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|expected
operator|.
name|getName
argument_list|()
argument_list|,
name|names
operator|.
name|nnb
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getQualifierAsString
argument_list|()
argument_list|,
name|names
operator|.
name|tn
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|expected
operator|.
name|getQualifier
argument_list|()
argument_list|,
name|names
operator|.
name|tnb
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getNamespaceAsString
argument_list|()
argument_list|,
name|names
operator|.
name|ns
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|expected
operator|.
name|getNamespace
argument_list|()
argument_list|,
name|names
operator|.
name|nsb
argument_list|)
expr_stmt|;
return|return
name|expected
return|;
block|}
block|}
end_class

end_unit
