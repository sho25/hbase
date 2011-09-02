begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**   * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|avro
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
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|Schema
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|generic
operator|.
name|GenericArray
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|generic
operator|.
name|GenericData
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
name|HBaseTestingUtility
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
name|avro
operator|.
name|generated
operator|.
name|AColumn
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
name|avro
operator|.
name|generated
operator|.
name|AColumnValue
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
name|avro
operator|.
name|generated
operator|.
name|AFamilyDescriptor
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
name|avro
operator|.
name|generated
operator|.
name|AGet
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
name|avro
operator|.
name|generated
operator|.
name|APut
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
name|avro
operator|.
name|generated
operator|.
name|ATableDescriptor
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Threads
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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

begin_comment
comment|/**  * Unit testing for AvroServer.HBaseImpl, a part of the  * org.apache.hadoop.hbase.avro package.  */
end_comment

begin_class
specifier|public
class|class
name|TestAvroServer
block|{
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
comment|// Static names for tables, columns, rows, and values
comment|// TODO(hammer): Better style to define these in test method?
specifier|private
specifier|static
name|ByteBuffer
name|tableAname
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"tableA"
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|ByteBuffer
name|tableBname
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"tableB"
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|ByteBuffer
name|familyAname
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"FamilyA"
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|ByteBuffer
name|qualifierAname
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"QualifierA"
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|ByteBuffer
name|rowAname
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"RowA"
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|ByteBuffer
name|valueA
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ValueA"
argument_list|)
argument_list|)
decl_stmt|;
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
block|}
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Nothing to do.
block|}
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Nothing to do.
block|}
comment|/**    * Tests for creating, enabling, disabling, modifying, and deleting tables.    *    * @throws Exception    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testTableAdminAndMetadata
parameter_list|()
throws|throws
name|Exception
block|{
name|AvroServer
operator|.
name|HBaseImpl
name|impl
init|=
operator|new
name|AvroServer
operator|.
name|HBaseImpl
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|impl
operator|.
name|listTables
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|ATableDescriptor
name|tableA
init|=
operator|new
name|ATableDescriptor
argument_list|()
decl_stmt|;
name|tableA
operator|.
name|name
operator|=
name|tableAname
expr_stmt|;
name|impl
operator|.
name|createTable
argument_list|(
name|tableA
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|impl
operator|.
name|listTables
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|impl
operator|.
name|isTableEnabled
argument_list|(
name|tableAname
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|impl
operator|.
name|tableExists
argument_list|(
name|tableAname
argument_list|)
argument_list|)
expr_stmt|;
name|ATableDescriptor
name|tableB
init|=
operator|new
name|ATableDescriptor
argument_list|()
decl_stmt|;
name|tableB
operator|.
name|name
operator|=
name|tableBname
expr_stmt|;
name|impl
operator|.
name|createTable
argument_list|(
name|tableB
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|impl
operator|.
name|listTables
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|impl
operator|.
name|disableTable
argument_list|(
name|tableBname
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|impl
operator|.
name|isTableEnabled
argument_list|(
name|tableBname
argument_list|)
argument_list|)
expr_stmt|;
name|impl
operator|.
name|deleteTable
argument_list|(
name|tableBname
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|impl
operator|.
name|listTables
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|impl
operator|.
name|disableTable
argument_list|(
name|tableAname
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|impl
operator|.
name|isTableEnabled
argument_list|(
name|tableAname
argument_list|)
argument_list|)
expr_stmt|;
name|tableA
operator|.
name|maxFileSize
operator|=
literal|123456L
expr_stmt|;
name|impl
operator|.
name|modifyTable
argument_list|(
name|tableAname
argument_list|,
name|tableA
argument_list|)
expr_stmt|;
comment|// It can take a while for the change to take effect.  Wait here a while.
while|while
condition|(
name|impl
operator|.
name|describeTable
argument_list|(
name|tableAname
argument_list|)
operator|==
literal|null
condition|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|impl
operator|.
name|describeTable
argument_list|(
name|tableAname
argument_list|)
operator|.
name|maxFileSize
operator|==
literal|123456L
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|123456L
argument_list|,
operator|(
name|long
operator|)
name|impl
operator|.
name|describeTable
argument_list|(
name|tableAname
argument_list|)
operator|.
name|maxFileSize
argument_list|)
expr_stmt|;
comment|/* DISABLED FOR NOW TILL WE HAVE BETTER DISABLE/ENABLE     impl.enableTable(tableAname);     assertTrue(impl.isTableEnabled(tableAname));          impl.disableTable(tableAname);     */
name|impl
operator|.
name|deleteTable
argument_list|(
name|tableAname
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests for creating, modifying, and deleting column families.    *    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testFamilyAdminAndMetadata
parameter_list|()
throws|throws
name|Exception
block|{
name|AvroServer
operator|.
name|HBaseImpl
name|impl
init|=
operator|new
name|AvroServer
operator|.
name|HBaseImpl
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|ATableDescriptor
name|tableA
init|=
operator|new
name|ATableDescriptor
argument_list|()
decl_stmt|;
name|tableA
operator|.
name|name
operator|=
name|tableAname
expr_stmt|;
name|AFamilyDescriptor
name|familyA
init|=
operator|new
name|AFamilyDescriptor
argument_list|()
decl_stmt|;
name|familyA
operator|.
name|name
operator|=
name|familyAname
expr_stmt|;
name|Schema
name|familyArraySchema
init|=
name|Schema
operator|.
name|createArray
argument_list|(
name|AFamilyDescriptor
operator|.
name|SCHEMA$
argument_list|)
decl_stmt|;
name|GenericArray
argument_list|<
name|AFamilyDescriptor
argument_list|>
name|families
init|=
operator|new
name|GenericData
operator|.
name|Array
argument_list|<
name|AFamilyDescriptor
argument_list|>
argument_list|(
literal|1
argument_list|,
name|familyArraySchema
argument_list|)
decl_stmt|;
name|families
operator|.
name|add
argument_list|(
name|familyA
argument_list|)
expr_stmt|;
name|tableA
operator|.
name|families
operator|=
name|families
expr_stmt|;
name|impl
operator|.
name|createTable
argument_list|(
name|tableA
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|impl
operator|.
name|describeTable
argument_list|(
name|tableAname
argument_list|)
operator|.
name|families
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|impl
operator|.
name|disableTable
argument_list|(
name|tableAname
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|impl
operator|.
name|isTableEnabled
argument_list|(
name|tableAname
argument_list|)
argument_list|)
expr_stmt|;
name|familyA
operator|.
name|maxVersions
operator|=
literal|123456
expr_stmt|;
name|impl
operator|.
name|modifyFamily
argument_list|(
name|tableAname
argument_list|,
name|familyAname
argument_list|,
name|familyA
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|int
operator|)
name|impl
operator|.
name|describeFamily
argument_list|(
name|tableAname
argument_list|,
name|familyAname
argument_list|)
operator|.
name|maxVersions
argument_list|,
literal|123456
argument_list|)
expr_stmt|;
name|impl
operator|.
name|deleteFamily
argument_list|(
name|tableAname
argument_list|,
name|familyAname
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|impl
operator|.
name|describeTable
argument_list|(
name|tableAname
argument_list|)
operator|.
name|families
operator|.
name|size
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|impl
operator|.
name|deleteTable
argument_list|(
name|tableAname
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests for adding, reading, and deleting data.    *    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testDML
parameter_list|()
throws|throws
name|Exception
block|{
name|AvroServer
operator|.
name|HBaseImpl
name|impl
init|=
operator|new
name|AvroServer
operator|.
name|HBaseImpl
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|ATableDescriptor
name|tableA
init|=
operator|new
name|ATableDescriptor
argument_list|()
decl_stmt|;
name|tableA
operator|.
name|name
operator|=
name|tableAname
expr_stmt|;
name|AFamilyDescriptor
name|familyA
init|=
operator|new
name|AFamilyDescriptor
argument_list|()
decl_stmt|;
name|familyA
operator|.
name|name
operator|=
name|familyAname
expr_stmt|;
name|Schema
name|familyArraySchema
init|=
name|Schema
operator|.
name|createArray
argument_list|(
name|AFamilyDescriptor
operator|.
name|SCHEMA$
argument_list|)
decl_stmt|;
name|GenericArray
argument_list|<
name|AFamilyDescriptor
argument_list|>
name|families
init|=
operator|new
name|GenericData
operator|.
name|Array
argument_list|<
name|AFamilyDescriptor
argument_list|>
argument_list|(
literal|1
argument_list|,
name|familyArraySchema
argument_list|)
decl_stmt|;
name|families
operator|.
name|add
argument_list|(
name|familyA
argument_list|)
expr_stmt|;
name|tableA
operator|.
name|families
operator|=
name|families
expr_stmt|;
name|impl
operator|.
name|createTable
argument_list|(
name|tableA
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|impl
operator|.
name|describeTable
argument_list|(
name|tableAname
argument_list|)
operator|.
name|families
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|AGet
name|getA
init|=
operator|new
name|AGet
argument_list|()
decl_stmt|;
name|getA
operator|.
name|row
operator|=
name|rowAname
expr_stmt|;
name|Schema
name|columnsSchema
init|=
name|Schema
operator|.
name|createArray
argument_list|(
name|AColumn
operator|.
name|SCHEMA$
argument_list|)
decl_stmt|;
name|GenericArray
argument_list|<
name|AColumn
argument_list|>
name|columns
init|=
operator|new
name|GenericData
operator|.
name|Array
argument_list|<
name|AColumn
argument_list|>
argument_list|(
literal|1
argument_list|,
name|columnsSchema
argument_list|)
decl_stmt|;
name|AColumn
name|column
init|=
operator|new
name|AColumn
argument_list|()
decl_stmt|;
name|column
operator|.
name|family
operator|=
name|familyAname
expr_stmt|;
name|column
operator|.
name|qualifier
operator|=
name|qualifierAname
expr_stmt|;
name|columns
operator|.
name|add
argument_list|(
name|column
argument_list|)
expr_stmt|;
name|getA
operator|.
name|columns
operator|=
name|columns
expr_stmt|;
name|assertFalse
argument_list|(
name|impl
operator|.
name|exists
argument_list|(
name|tableAname
argument_list|,
name|getA
argument_list|)
argument_list|)
expr_stmt|;
name|APut
name|putA
init|=
operator|new
name|APut
argument_list|()
decl_stmt|;
name|putA
operator|.
name|row
operator|=
name|rowAname
expr_stmt|;
name|Schema
name|columnValuesSchema
init|=
name|Schema
operator|.
name|createArray
argument_list|(
name|AColumnValue
operator|.
name|SCHEMA$
argument_list|)
decl_stmt|;
name|GenericArray
argument_list|<
name|AColumnValue
argument_list|>
name|columnValues
init|=
operator|new
name|GenericData
operator|.
name|Array
argument_list|<
name|AColumnValue
argument_list|>
argument_list|(
literal|1
argument_list|,
name|columnValuesSchema
argument_list|)
decl_stmt|;
name|AColumnValue
name|acv
init|=
operator|new
name|AColumnValue
argument_list|()
decl_stmt|;
name|acv
operator|.
name|family
operator|=
name|familyAname
expr_stmt|;
name|acv
operator|.
name|qualifier
operator|=
name|qualifierAname
expr_stmt|;
name|acv
operator|.
name|value
operator|=
name|valueA
expr_stmt|;
name|columnValues
operator|.
name|add
argument_list|(
name|acv
argument_list|)
expr_stmt|;
name|putA
operator|.
name|columnValues
operator|=
name|columnValues
expr_stmt|;
name|impl
operator|.
name|put
argument_list|(
name|tableAname
argument_list|,
name|putA
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|impl
operator|.
name|exists
argument_list|(
name|tableAname
argument_list|,
name|getA
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|impl
operator|.
name|get
argument_list|(
name|tableAname
argument_list|,
name|getA
argument_list|)
operator|.
name|entries
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|impl
operator|.
name|disableTable
argument_list|(
name|tableAname
argument_list|)
expr_stmt|;
name|impl
operator|.
name|deleteTable
argument_list|(
name|tableAname
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

