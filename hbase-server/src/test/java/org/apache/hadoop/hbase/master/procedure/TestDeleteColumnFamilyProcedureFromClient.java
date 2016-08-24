begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|master
operator|.
name|procedure
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
name|io
operator|.
name|IOException
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
name|FileStatus
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
name|fs
operator|.
name|PathFilter
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
name|HColumnDescriptor
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
name|HConstants
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
name|HTableDescriptor
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
name|InvalidFamilyOperationException
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
name|Admin
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
name|testclassification
operator|.
name|LargeTests
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
name|MasterTests
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
name|wal
operator|.
name|WALSplitter
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
name|Assert
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MasterTests
operator|.
name|class
block|,
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestDeleteColumnFamilyProcedureFromClient
block|{
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|TABLENAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"column_family_handlers"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|FAMILIES
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf1"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf2"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf3"
argument_list|)
block|}
decl_stmt|;
comment|/**    * Start up a mini cluster and put a small table of empty regions into it.    *    * @throws Exception    */
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeAllTests
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterAllTests
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
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
comment|// Create a table of three families. This will assign a region.
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLENAME
argument_list|,
name|FAMILIES
argument_list|)
expr_stmt|;
name|Table
name|t
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TABLENAME
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|waitUntilNoRegionsInTransition
argument_list|()
expr_stmt|;
comment|// Load the table with data for all families
name|TEST_UTIL
operator|.
name|loadTable
argument_list|(
name|t
argument_list|,
name|FAMILIES
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|flush
argument_list|()
expr_stmt|;
name|t
operator|.
name|close
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|ensureSomeRegionServersAvailable
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|cleanup
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|TABLENAME
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|deleteColumnFamilyWithMultipleRegions
parameter_list|()
throws|throws
name|Exception
block|{
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|HTableDescriptor
name|beforehtd
init|=
name|admin
operator|.
name|getTableDescriptor
argument_list|(
name|TABLENAME
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|TEST_UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
comment|// 1 - Check if table exists in descriptor
name|assertTrue
argument_list|(
name|admin
operator|.
name|isTableAvailable
argument_list|(
name|TABLENAME
argument_list|)
argument_list|)
expr_stmt|;
comment|// 2 - Check if all three families exist in descriptor
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|beforehtd
operator|.
name|getColumnFamilies
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|HColumnDescriptor
index|[]
name|families
init|=
name|beforehtd
operator|.
name|getColumnFamilies
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|families
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|assertTrue
argument_list|(
name|families
index|[
name|i
index|]
operator|.
name|getNameAsString
argument_list|()
operator|.
name|equals
argument_list|(
literal|"cf"
operator|+
operator|(
name|i
operator|+
literal|1
operator|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// 3 - Check if table exists in FS
name|Path
name|tableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|TEST_UTIL
operator|.
name|getDefaultRootDirPath
argument_list|()
argument_list|,
name|TABLENAME
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|tableDir
argument_list|)
argument_list|)
expr_stmt|;
comment|// 4 - Check if all the 3 column families exist in FS
name|FileStatus
index|[]
name|fileStatus
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|tableDir
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|fileStatus
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|fileStatus
index|[
name|i
index|]
operator|.
name|isDirectory
argument_list|()
operator|==
literal|true
condition|)
block|{
name|FileStatus
index|[]
name|cf
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|fileStatus
index|[
name|i
index|]
operator|.
name|getPath
argument_list|()
argument_list|,
operator|new
name|PathFilter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|p
parameter_list|)
block|{
if|if
condition|(
name|p
operator|.
name|getName
argument_list|()
operator|.
name|contains
argument_list|(
name|HConstants
operator|.
name|RECOVERED_EDITS_DIR
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
argument_list|)
decl_stmt|;
name|int
name|k
init|=
literal|1
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|cf
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
if|if
condition|(
name|cf
index|[
name|j
index|]
operator|.
name|isDirectory
argument_list|()
operator|==
literal|true
operator|&&
name|cf
index|[
name|j
index|]
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"."
argument_list|)
operator|==
literal|false
condition|)
block|{
name|assertEquals
argument_list|(
name|cf
index|[
name|j
index|]
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
literal|"cf"
operator|+
name|k
argument_list|)
expr_stmt|;
name|k
operator|++
expr_stmt|;
block|}
block|}
block|}
block|}
comment|// TEST - Disable and delete the column family
name|admin
operator|.
name|disableTable
argument_list|(
name|TABLENAME
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteColumnFamily
argument_list|(
name|TABLENAME
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf2"
argument_list|)
argument_list|)
expr_stmt|;
comment|// 5 - Check if only 2 column families exist in the descriptor
name|HTableDescriptor
name|afterhtd
init|=
name|admin
operator|.
name|getTableDescriptor
argument_list|(
name|TABLENAME
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|afterhtd
operator|.
name|getColumnFamilies
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|HColumnDescriptor
index|[]
name|newFamilies
init|=
name|afterhtd
operator|.
name|getColumnFamilies
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|newFamilies
index|[
literal|0
index|]
operator|.
name|getNameAsString
argument_list|()
operator|.
name|equals
argument_list|(
literal|"cf1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|newFamilies
index|[
literal|1
index|]
operator|.
name|getNameAsString
argument_list|()
operator|.
name|equals
argument_list|(
literal|"cf3"
argument_list|)
argument_list|)
expr_stmt|;
comment|// 6 - Check if the second column family is gone from the FS
name|fileStatus
operator|=
name|fs
operator|.
name|listStatus
argument_list|(
name|tableDir
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|fileStatus
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|fileStatus
index|[
name|i
index|]
operator|.
name|isDirectory
argument_list|()
operator|==
literal|true
condition|)
block|{
name|FileStatus
index|[]
name|cf
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|fileStatus
index|[
name|i
index|]
operator|.
name|getPath
argument_list|()
argument_list|,
operator|new
name|PathFilter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|p
parameter_list|)
block|{
if|if
condition|(
name|WALSplitter
operator|.
name|isSequenceIdFile
argument_list|(
name|p
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|cf
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
if|if
condition|(
name|cf
index|[
name|j
index|]
operator|.
name|isDirectory
argument_list|()
operator|==
literal|true
condition|)
block|{
name|assertFalse
argument_list|(
name|cf
index|[
name|j
index|]
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"cf2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|deleteColumnFamilyTwice
parameter_list|()
throws|throws
name|Exception
block|{
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|HTableDescriptor
name|beforehtd
init|=
name|admin
operator|.
name|getTableDescriptor
argument_list|(
name|TABLENAME
argument_list|)
decl_stmt|;
name|String
name|cfToDelete
init|=
literal|"cf1"
decl_stmt|;
name|FileSystem
name|fs
init|=
name|TEST_UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
comment|// 1 - Check if table exists in descriptor
name|assertTrue
argument_list|(
name|admin
operator|.
name|isTableAvailable
argument_list|(
name|TABLENAME
argument_list|)
argument_list|)
expr_stmt|;
comment|// 2 - Check if all the target column family exist in descriptor
name|HColumnDescriptor
index|[]
name|families
init|=
name|beforehtd
operator|.
name|getColumnFamilies
argument_list|()
decl_stmt|;
name|Boolean
name|foundCF
init|=
literal|false
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|families
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|families
index|[
name|i
index|]
operator|.
name|getNameAsString
argument_list|()
operator|.
name|equals
argument_list|(
name|cfToDelete
argument_list|)
condition|)
block|{
name|foundCF
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
name|assertTrue
argument_list|(
name|foundCF
argument_list|)
expr_stmt|;
comment|// 3 - Check if table exists in FS
name|Path
name|tableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|TEST_UTIL
operator|.
name|getDefaultRootDirPath
argument_list|()
argument_list|,
name|TABLENAME
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|tableDir
argument_list|)
argument_list|)
expr_stmt|;
comment|// 4 - Check if all the target column family exist in FS
name|FileStatus
index|[]
name|fileStatus
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|tableDir
argument_list|)
decl_stmt|;
name|foundCF
operator|=
literal|false
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|fileStatus
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|fileStatus
index|[
name|i
index|]
operator|.
name|isDirectory
argument_list|()
operator|==
literal|true
condition|)
block|{
name|FileStatus
index|[]
name|cf
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|fileStatus
index|[
name|i
index|]
operator|.
name|getPath
argument_list|()
argument_list|,
operator|new
name|PathFilter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|p
parameter_list|)
block|{
if|if
condition|(
name|p
operator|.
name|getName
argument_list|()
operator|.
name|contains
argument_list|(
name|HConstants
operator|.
name|RECOVERED_EDITS_DIR
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|cf
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
if|if
condition|(
name|cf
index|[
name|j
index|]
operator|.
name|isDirectory
argument_list|()
operator|==
literal|true
operator|&&
name|cf
index|[
name|j
index|]
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|cfToDelete
argument_list|)
condition|)
block|{
name|foundCF
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
block|}
if|if
condition|(
name|foundCF
condition|)
block|{
break|break;
block|}
block|}
name|assertTrue
argument_list|(
name|foundCF
argument_list|)
expr_stmt|;
comment|// TEST - Disable and delete the column family
if|if
condition|(
name|admin
operator|.
name|isTableEnabled
argument_list|(
name|TABLENAME
argument_list|)
condition|)
block|{
name|admin
operator|.
name|disableTable
argument_list|(
name|TABLENAME
argument_list|)
expr_stmt|;
block|}
name|admin
operator|.
name|deleteColumnFamily
argument_list|(
name|TABLENAME
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|cfToDelete
argument_list|)
argument_list|)
expr_stmt|;
comment|// 5 - Check if the target column family is gone from the FS
name|fileStatus
operator|=
name|fs
operator|.
name|listStatus
argument_list|(
name|tableDir
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|fileStatus
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|fileStatus
index|[
name|i
index|]
operator|.
name|isDirectory
argument_list|()
operator|==
literal|true
condition|)
block|{
name|FileStatus
index|[]
name|cf
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|fileStatus
index|[
name|i
index|]
operator|.
name|getPath
argument_list|()
argument_list|,
operator|new
name|PathFilter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|p
parameter_list|)
block|{
if|if
condition|(
name|WALSplitter
operator|.
name|isSequenceIdFile
argument_list|(
name|p
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|cf
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
if|if
condition|(
name|cf
index|[
name|j
index|]
operator|.
name|isDirectory
argument_list|()
operator|==
literal|true
condition|)
block|{
name|assertFalse
argument_list|(
name|cf
index|[
name|j
index|]
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|cfToDelete
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
try|try
block|{
comment|// Test: delete again
name|admin
operator|.
name|deleteColumnFamily
argument_list|(
name|TABLENAME
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|cfToDelete
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
literal|"Delete a non-exist column family should fail"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvalidFamilyOperationException
name|e
parameter_list|)
block|{
comment|// Expected.
block|}
block|}
block|}
end_class

end_unit

