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
name|assertFalse
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
name|assertTrue
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|client
operator|.
name|Durability
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
name|exceptions
operator|.
name|DeserializationException
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
name|SmallTests
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
name|BuilderStyleTest
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

begin_comment
comment|/**  * Test setting values in the descriptor  */
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
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestHTableDescriptor
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestHTableDescriptor
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IOException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testAddCoprocessorTwice
parameter_list|()
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
name|String
name|cpName
init|=
literal|"a.b.c.d"
decl_stmt|;
name|htd
operator|.
name|addCoprocessor
argument_list|(
name|cpName
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addCoprocessor
argument_list|(
name|cpName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAddCoprocessorWithSpecStr
parameter_list|()
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
name|String
name|cpName
init|=
literal|"a.b.c.d"
decl_stmt|;
try|try
block|{
name|htd
operator|.
name|addCoprocessorWithSpec
argument_list|(
name|cpName
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|iae
parameter_list|)
block|{
comment|// Expected as cpName is invalid
block|}
comment|// Try minimal spec.
try|try
block|{
name|htd
operator|.
name|addCoprocessorWithSpec
argument_list|(
literal|"file:///some/path"
operator|+
literal|"|"
operator|+
name|cpName
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|iae
parameter_list|)
block|{
comment|// Expected to be invalid
block|}
comment|// Try more spec.
name|String
name|spec
init|=
literal|"hdfs:///foo.jar|com.foo.FooRegionObserver|1001|arg1=1,arg2=2"
decl_stmt|;
try|try
block|{
name|htd
operator|.
name|addCoprocessorWithSpec
argument_list|(
name|spec
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|iae
parameter_list|)
block|{
name|fail
argument_list|()
expr_stmt|;
block|}
comment|// Try double add of same coprocessor
try|try
block|{
name|htd
operator|.
name|addCoprocessorWithSpec
argument_list|(
name|spec
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
comment|// Expect that the coprocessor already exists
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPb
parameter_list|()
throws|throws
name|DeserializationException
throws|,
name|IOException
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
specifier|final
name|int
name|v
init|=
literal|123
decl_stmt|;
name|htd
operator|.
name|setMaxFileSize
argument_list|(
name|v
argument_list|)
expr_stmt|;
name|htd
operator|.
name|setDurability
argument_list|(
name|Durability
operator|.
name|ASYNC_WAL
argument_list|)
expr_stmt|;
name|htd
operator|.
name|setReadOnly
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|htd
operator|.
name|setRegionReplication
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|byte
index|[]
name|bytes
init|=
name|htd
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|HTableDescriptor
name|deserializedHtd
init|=
name|HTableDescriptor
operator|.
name|parseFrom
argument_list|(
name|bytes
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|htd
argument_list|,
name|deserializedHtd
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|v
argument_list|,
name|deserializedHtd
operator|.
name|getMaxFileSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|deserializedHtd
operator|.
name|isReadOnly
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Durability
operator|.
name|ASYNC_WAL
argument_list|,
name|deserializedHtd
operator|.
name|getDurability
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|deserializedHtd
operator|.
name|getRegionReplication
argument_list|()
argument_list|,
literal|2
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test cps in the table description    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testGetSetRemoveCP
parameter_list|()
throws|throws
name|Exception
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"table"
argument_list|)
argument_list|)
decl_stmt|;
comment|// simple CP
name|String
name|className
init|=
literal|"org.apache.hadoop.hbase.coprocessor.BaseRegionObserver"
decl_stmt|;
comment|// add and check that it is present
name|desc
operator|.
name|addCoprocessor
argument_list|(
name|className
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|desc
operator|.
name|hasCoprocessor
argument_list|(
name|className
argument_list|)
argument_list|)
expr_stmt|;
comment|// remove it and check that it is gone
name|desc
operator|.
name|removeCoprocessor
argument_list|(
name|className
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|desc
operator|.
name|hasCoprocessor
argument_list|(
name|className
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test cps in the table description    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testSetListRemoveCP
parameter_list|()
throws|throws
name|Exception
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testGetSetRemoveCP"
argument_list|)
argument_list|)
decl_stmt|;
comment|// simple CP
name|String
name|className1
init|=
literal|"org.apache.hadoop.hbase.coprocessor.BaseRegionObserver"
decl_stmt|;
name|String
name|className2
init|=
literal|"org.apache.hadoop.hbase.coprocessor.SampleRegionWALObserver"
decl_stmt|;
comment|// Check that any coprocessor is present.
name|assertTrue
argument_list|(
name|desc
operator|.
name|getCoprocessors
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|0
argument_list|)
expr_stmt|;
comment|// Add the 1 coprocessor and check if present.
name|desc
operator|.
name|addCoprocessor
argument_list|(
name|className1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|desc
operator|.
name|getCoprocessors
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|desc
operator|.
name|getCoprocessors
argument_list|()
operator|.
name|contains
argument_list|(
name|className1
argument_list|)
argument_list|)
expr_stmt|;
comment|// Add the 2nd coprocessor and check if present.
comment|// remove it and check that it is gone
name|desc
operator|.
name|addCoprocessor
argument_list|(
name|className2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|desc
operator|.
name|getCoprocessors
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|desc
operator|.
name|getCoprocessors
argument_list|()
operator|.
name|contains
argument_list|(
name|className2
argument_list|)
argument_list|)
expr_stmt|;
comment|// Remove one and check
name|desc
operator|.
name|removeCoprocessor
argument_list|(
name|className1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|desc
operator|.
name|getCoprocessors
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|desc
operator|.
name|getCoprocessors
argument_list|()
operator|.
name|contains
argument_list|(
name|className1
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|desc
operator|.
name|getCoprocessors
argument_list|()
operator|.
name|contains
argument_list|(
name|className2
argument_list|)
argument_list|)
expr_stmt|;
comment|// Remove the last and check
name|desc
operator|.
name|removeCoprocessor
argument_list|(
name|className2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|desc
operator|.
name|getCoprocessors
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|0
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|desc
operator|.
name|getCoprocessors
argument_list|()
operator|.
name|contains
argument_list|(
name|className1
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|desc
operator|.
name|getCoprocessors
argument_list|()
operator|.
name|contains
argument_list|(
name|className2
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that we add and remove strings from settings properly.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testRemoveString
parameter_list|()
throws|throws
name|Exception
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"table"
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|key
init|=
literal|"Some"
decl_stmt|;
name|String
name|value
init|=
literal|"value"
decl_stmt|;
name|desc
operator|.
name|setValue
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|value
argument_list|,
name|desc
operator|.
name|getValue
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
name|desc
operator|.
name|remove
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|desc
operator|.
name|getValue
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
annotation|@
name|Test
specifier|public
name|void
name|testLegalHTableNamesRegex
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
name|tName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|tn
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Testing: '"
operator|+
name|tn
operator|+
literal|"'"
argument_list|,
name|Pattern
operator|.
name|matches
argument_list|(
name|TableName
operator|.
name|VALID_USER_TABLE_REGEX
argument_list|,
name|tName
operator|.
name|getNameAsString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIllegalHTableNamesRegex
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Testing: '"
operator|+
name|tn
operator|+
literal|"'"
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|Pattern
operator|.
name|matches
argument_list|(
name|TableName
operator|.
name|VALID_USER_TABLE_REGEX
argument_list|,
name|tn
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Test default value handling for maxFileSize    */
annotation|@
name|Test
specifier|public
name|void
name|testGetMaxFileSize
parameter_list|()
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"table"
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|desc
operator|.
name|getMaxFileSize
argument_list|()
argument_list|)
expr_stmt|;
name|desc
operator|.
name|setMaxFileSize
argument_list|(
literal|1111L
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1111L
argument_list|,
name|desc
operator|.
name|getMaxFileSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test default value handling for memStoreFlushSize    */
annotation|@
name|Test
specifier|public
name|void
name|testGetMemStoreFlushSize
parameter_list|()
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"table"
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|desc
operator|.
name|getMemStoreFlushSize
argument_list|()
argument_list|)
expr_stmt|;
name|desc
operator|.
name|setMemStoreFlushSize
argument_list|(
literal|1111L
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1111L
argument_list|,
name|desc
operator|.
name|getMemStoreFlushSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that we add and remove strings from configuration properly.    */
annotation|@
name|Test
specifier|public
name|void
name|testAddGetRemoveConfiguration
parameter_list|()
throws|throws
name|Exception
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"table"
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|key
init|=
literal|"Some"
decl_stmt|;
name|String
name|value
init|=
literal|"value"
decl_stmt|;
name|desc
operator|.
name|setConfiguration
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|value
argument_list|,
name|desc
operator|.
name|getConfigurationValue
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
name|desc
operator|.
name|removeConfiguration
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|desc
operator|.
name|getConfigurationValue
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testClassMethodsAreBuilderStyle
parameter_list|()
block|{
comment|/* HTableDescriptor should have a builder style setup where setXXX/addXXX methods      * can be chainable together:      * . For example:      * HTableDescriptor htd      *   = new HTableDescriptor()      *     .setFoo(foo)      *     .setBar(bar)      *     .setBuz(buz)      *      * This test ensures that all methods starting with "set" returns the declaring object      */
name|BuilderStyleTest
operator|.
name|assertClassesAreBuilderStyle
argument_list|(
name|HTableDescriptor
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testModifyFamily
parameter_list|()
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"table"
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|familyName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|familyName
argument_list|)
decl_stmt|;
name|hcd
operator|.
name|setBlocksize
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setDFSReplication
argument_list|(
operator|(
name|short
operator|)
literal|3
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1000
argument_list|,
name|htd
operator|.
name|getFamily
argument_list|(
name|familyName
argument_list|)
operator|.
name|getBlocksize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|htd
operator|.
name|getFamily
argument_list|(
name|familyName
argument_list|)
operator|.
name|getDFSReplication
argument_list|()
argument_list|)
expr_stmt|;
name|hcd
operator|=
operator|new
name|HColumnDescriptor
argument_list|(
name|familyName
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setBlocksize
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setDFSReplication
argument_list|(
operator|(
name|short
operator|)
literal|1
argument_list|)
expr_stmt|;
name|htd
operator|.
name|modifyFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2000
argument_list|,
name|htd
operator|.
name|getFamily
argument_list|(
name|familyName
argument_list|)
operator|.
name|getBlocksize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|htd
operator|.
name|getFamily
argument_list|(
name|familyName
argument_list|)
operator|.
name|getDFSReplication
argument_list|()
argument_list|)
expr_stmt|;
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
name|testModifyInexistentFamily
parameter_list|()
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"table"
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|familyName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|familyName
argument_list|)
decl_stmt|;
name|htd
operator|.
name|modifyFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
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
name|testAddDuplicateFamilies
parameter_list|()
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"table"
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|familyName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|familyName
argument_list|)
decl_stmt|;
name|hcd
operator|.
name|setBlocksize
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1000
argument_list|,
name|htd
operator|.
name|getFamily
argument_list|(
name|familyName
argument_list|)
operator|.
name|getBlocksize
argument_list|()
argument_list|)
expr_stmt|;
name|hcd
operator|=
operator|new
name|HColumnDescriptor
argument_list|(
name|familyName
argument_list|)
expr_stmt|;
name|hcd
operator|.
name|setBlocksize
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

