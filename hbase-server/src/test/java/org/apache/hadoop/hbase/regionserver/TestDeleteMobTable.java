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
operator|.
name|regionserver
package|;
end_package

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
name|Random
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|HBaseClassTestRule
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
name|client
operator|.
name|ColumnFamilyDescriptor
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
name|ColumnFamilyDescriptorBuilder
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
name|Put
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
name|Result
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
name|ResultScanner
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
name|Scan
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
name|Table
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
name|TableDescriptor
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
name|TableDescriptorBuilder
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
name|mob
operator|.
name|MobConstants
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
name|mob
operator|.
name|MobUtils
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
name|EnvironmentEdgeManager
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
name|FSUtils
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
name|HFileArchiveUtil
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
name|Assert
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
name|ClassRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
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
name|TestName
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestDeleteMobTable
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestDeleteMobTable
operator|.
name|class
argument_list|)
decl_stmt|;
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
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
index|[]
name|QF
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qualifier"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Random
name|random
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
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
literal|1
argument_list|)
expr_stmt|;
block|}
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
comment|/**    * Generate the mob value.    *    * @param size    *          the size of the value    * @return the mob value generated    */
specifier|private
specifier|static
name|byte
index|[]
name|generateMobValue
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|byte
index|[]
name|mobVal
init|=
operator|new
name|byte
index|[
name|size
index|]
decl_stmt|;
name|random
operator|.
name|nextBytes
argument_list|(
name|mobVal
argument_list|)
expr_stmt|;
return|return
name|mobVal
return|;
block|}
specifier|private
name|TableDescriptorBuilder
operator|.
name|ModifyableTableDescriptor
name|createTableDescriptor
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|boolean
name|hasMob
parameter_list|)
block|{
name|TableDescriptorBuilder
operator|.
name|ModifyableTableDescriptor
name|tableDescriptor
init|=
operator|new
name|TableDescriptorBuilder
operator|.
name|ModifyableTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|ColumnFamilyDescriptorBuilder
operator|.
name|ModifyableColumnFamilyDescriptor
name|familyDescriptor
init|=
operator|new
name|ColumnFamilyDescriptorBuilder
operator|.
name|ModifyableColumnFamilyDescriptor
argument_list|(
name|FAMILY
argument_list|)
decl_stmt|;
if|if
condition|(
name|hasMob
condition|)
block|{
name|familyDescriptor
operator|.
name|setMobEnabled
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|familyDescriptor
operator|.
name|setMobThreshold
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|tableDescriptor
operator|.
name|setColumnFamily
argument_list|(
name|familyDescriptor
argument_list|)
expr_stmt|;
return|return
name|tableDescriptor
return|;
block|}
specifier|private
name|Table
name|createTableWithOneFile
parameter_list|(
name|TableDescriptor
name|tableDescriptor
parameter_list|)
throws|throws
name|IOException
block|{
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableDescriptor
argument_list|,
literal|null
argument_list|)
decl_stmt|;
try|try
block|{
comment|// insert data
name|byte
index|[]
name|value
init|=
name|generateMobValue
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QF
argument_list|,
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
comment|// create an hfile
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|flush
argument_list|(
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
return|return
name|table
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeleteMobTable
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|TableDescriptor
name|tableDescriptor
init|=
name|createTableDescriptor
argument_list|(
name|tableName
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|ColumnFamilyDescriptor
name|familyDescriptor
init|=
name|tableDescriptor
operator|.
name|getColumnFamily
argument_list|(
name|FAMILY
argument_list|)
decl_stmt|;
name|String
name|fileName
init|=
literal|null
decl_stmt|;
name|Table
name|table
init|=
name|createTableWithOneFile
argument_list|(
name|tableDescriptor
argument_list|)
decl_stmt|;
try|try
block|{
comment|// the mob file exists
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|countMobFiles
argument_list|(
name|tableName
argument_list|,
name|familyDescriptor
operator|.
name|getNameAsString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|countArchiveMobFiles
argument_list|(
name|tableName
argument_list|,
name|familyDescriptor
operator|.
name|getNameAsString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|fileName
operator|=
name|assertHasOneMobRow
argument_list|(
name|table
argument_list|,
name|tableName
argument_list|,
name|familyDescriptor
operator|.
name|getNameAsString
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|mobArchiveExist
argument_list|(
name|tableName
argument_list|,
name|familyDescriptor
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|fileName
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|mobTableDirExist
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertFalse
argument_list|(
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|tableExists
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|countMobFiles
argument_list|(
name|tableName
argument_list|,
name|familyDescriptor
operator|.
name|getNameAsString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|countArchiveMobFiles
argument_list|(
name|tableName
argument_list|,
name|familyDescriptor
operator|.
name|getNameAsString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|mobArchiveExist
argument_list|(
name|tableName
argument_list|,
name|familyDescriptor
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|fileName
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|mobTableDirExist
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeleteNonMobTable
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|TableDescriptor
name|htd
init|=
name|createTableDescriptor
argument_list|(
name|tableName
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|ColumnFamilyDescriptor
name|hcd
init|=
name|htd
operator|.
name|getColumnFamily
argument_list|(
name|FAMILY
argument_list|)
decl_stmt|;
name|Table
name|table
init|=
name|createTableWithOneFile
argument_list|(
name|htd
argument_list|)
decl_stmt|;
try|try
block|{
comment|// the mob file doesn't exist
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|countMobFiles
argument_list|(
name|tableName
argument_list|,
name|hcd
operator|.
name|getNameAsString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|countArchiveMobFiles
argument_list|(
name|tableName
argument_list|,
name|hcd
operator|.
name|getNameAsString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|mobTableDirExist
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertFalse
argument_list|(
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|tableExists
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|countMobFiles
argument_list|(
name|tableName
argument_list|,
name|hcd
operator|.
name|getNameAsString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|countArchiveMobFiles
argument_list|(
name|tableName
argument_list|,
name|hcd
operator|.
name|getNameAsString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|mobTableDirExist
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMobFamilyDelete
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|TableDescriptorBuilder
operator|.
name|ModifyableTableDescriptor
name|tableDescriptor
init|=
name|createTableDescriptor
argument_list|(
name|tableName
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|ColumnFamilyDescriptor
name|familyDescriptor
init|=
name|tableDescriptor
operator|.
name|getColumnFamily
argument_list|(
name|FAMILY
argument_list|)
decl_stmt|;
name|tableDescriptor
operator|.
name|setColumnFamily
argument_list|(
operator|new
name|ColumnFamilyDescriptorBuilder
operator|.
name|ModifyableColumnFamilyDescriptor
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family2"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|createTableWithOneFile
argument_list|(
name|tableDescriptor
argument_list|)
decl_stmt|;
try|try
block|{
comment|// the mob file exists
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|countMobFiles
argument_list|(
name|tableName
argument_list|,
name|familyDescriptor
operator|.
name|getNameAsString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|countArchiveMobFiles
argument_list|(
name|tableName
argument_list|,
name|familyDescriptor
operator|.
name|getNameAsString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|fileName
init|=
name|assertHasOneMobRow
argument_list|(
name|table
argument_list|,
name|tableName
argument_list|,
name|familyDescriptor
operator|.
name|getNameAsString
argument_list|()
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|mobArchiveExist
argument_list|(
name|tableName
argument_list|,
name|familyDescriptor
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|fileName
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|mobTableDirExist
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|deleteColumnFamily
argument_list|(
name|tableName
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|countMobFiles
argument_list|(
name|tableName
argument_list|,
name|familyDescriptor
operator|.
name|getNameAsString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|countArchiveMobFiles
argument_list|(
name|tableName
argument_list|,
name|familyDescriptor
operator|.
name|getNameAsString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|mobArchiveExist
argument_list|(
name|tableName
argument_list|,
name|familyDescriptor
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|fileName
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|mobColumnFamilyDirExist
argument_list|(
name|tableName
argument_list|,
name|familyDescriptor
operator|.
name|getNameAsString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|int
name|countMobFiles
parameter_list|(
name|TableName
name|tn
parameter_list|,
name|String
name|familyName
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
name|Path
name|mobFileDir
init|=
name|MobUtils
operator|.
name|getMobFamilyPath
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tn
argument_list|,
name|familyName
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|mobFileDir
argument_list|)
condition|)
block|{
return|return
name|fs
operator|.
name|listStatus
argument_list|(
name|mobFileDir
argument_list|)
operator|.
name|length
return|;
block|}
return|return
literal|0
return|;
block|}
specifier|private
name|int
name|countArchiveMobFiles
parameter_list|(
name|TableName
name|tn
parameter_list|,
name|String
name|familyName
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
name|Path
name|storePath
init|=
name|HFileArchiveUtil
operator|.
name|getStoreArchivePath
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tn
argument_list|,
name|MobUtils
operator|.
name|getMobRegionInfo
argument_list|(
name|tn
argument_list|)
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|familyName
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|storePath
argument_list|)
condition|)
block|{
return|return
name|fs
operator|.
name|listStatus
argument_list|(
name|storePath
argument_list|)
operator|.
name|length
return|;
block|}
return|return
literal|0
return|;
block|}
specifier|private
name|boolean
name|mobTableDirExist
parameter_list|(
name|TableName
name|tn
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
name|Path
name|tableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|MobUtils
operator|.
name|getMobHome
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|tn
argument_list|)
decl_stmt|;
return|return
name|fs
operator|.
name|exists
argument_list|(
name|tableDir
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|mobColumnFamilyDirExist
parameter_list|(
name|TableName
name|tn
parameter_list|,
name|String
name|familyName
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
name|Path
name|mobFamilyDir
init|=
name|MobUtils
operator|.
name|getMobFamilyPath
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tn
argument_list|,
name|familyName
argument_list|)
decl_stmt|;
return|return
name|fs
operator|.
name|exists
argument_list|(
name|mobFamilyDir
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|mobArchiveExist
parameter_list|(
name|TableName
name|tn
parameter_list|,
name|String
name|familyName
parameter_list|,
name|String
name|fileName
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
name|Path
name|storePath
init|=
name|HFileArchiveUtil
operator|.
name|getStoreArchivePath
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tn
argument_list|,
name|MobUtils
operator|.
name|getMobRegionInfo
argument_list|(
name|tn
argument_list|)
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|familyName
argument_list|)
decl_stmt|;
return|return
name|fs
operator|.
name|exists
argument_list|(
operator|new
name|Path
argument_list|(
name|storePath
argument_list|,
name|fileName
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|String
name|assertHasOneMobRow
parameter_list|(
name|Table
name|table
parameter_list|,
name|TableName
name|tn
parameter_list|,
name|String
name|familyName
parameter_list|)
throws|throws
name|IOException
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setAttribute
argument_list|(
name|MobConstants
operator|.
name|MOB_SCAN_RAW
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|)
argument_list|)
expr_stmt|;
name|ResultScanner
name|rs
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|Result
name|r
init|=
name|rs
operator|.
name|next
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|r
argument_list|)
expr_stmt|;
name|String
name|fileName
init|=
name|MobUtils
operator|.
name|getMobFileName
argument_list|(
name|r
operator|.
name|getColumnLatestCell
argument_list|(
name|FAMILY
argument_list|,
name|QF
argument_list|)
argument_list|)
decl_stmt|;
name|Path
name|filePath
init|=
operator|new
name|Path
argument_list|(
name|MobUtils
operator|.
name|getMobFamilyPath
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tn
argument_list|,
name|familyName
argument_list|)
argument_list|,
name|fileName
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|filePath
argument_list|)
argument_list|)
expr_stmt|;
name|r
operator|=
name|rs
operator|.
name|next
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|r
argument_list|)
expr_stmt|;
return|return
name|fileName
return|;
block|}
block|}
end_class

end_unit

