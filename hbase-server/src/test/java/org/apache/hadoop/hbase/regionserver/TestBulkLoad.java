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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
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
name|FSDataOutputStream
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
name|CellUtil
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
name|DoNotRetryIOException
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
name|HBaseConfiguration
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
name|HRegionInfo
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
name|KeyValue
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
name|io
operator|.
name|hfile
operator|.
name|HFile
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
name|io
operator|.
name|hfile
operator|.
name|HFileContext
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
name|protobuf
operator|.
name|ProtobufUtil
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
name|protobuf
operator|.
name|generated
operator|.
name|WALProtos
operator|.
name|BulkLoadDescriptor
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
name|protobuf
operator|.
name|generated
operator|.
name|WALProtos
operator|.
name|StoreDescriptor
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
name|regionserver
operator|.
name|wal
operator|.
name|WALEdit
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
name|Pair
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
name|WAL
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
name|WALKey
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|Description
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|TypeSafeMatcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jmock
operator|.
name|Expectations
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jmock
operator|.
name|integration
operator|.
name|junit4
operator|.
name|JUnitRuleMockery
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jmock
operator|.
name|lib
operator|.
name|concurrent
operator|.
name|Synchroniser
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
name|TemporaryFolder
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

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileOutputStream
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Arrays
operator|.
name|asList
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
name|assertNotNull
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

begin_comment
comment|/**  * This class attempts to unit test bulk HLog loading.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestBulkLoad
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
name|TemporaryFolder
name|testFolder
init|=
operator|new
name|TemporaryFolder
argument_list|()
decl_stmt|;
annotation|@
name|Rule
specifier|public
specifier|final
name|JUnitRuleMockery
name|context
init|=
operator|new
name|JUnitRuleMockery
argument_list|()
block|{
block|{
name|setThreadingPolicy
argument_list|(
operator|new
name|Synchroniser
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
specifier|private
specifier|final
name|WAL
name|log
init|=
name|context
operator|.
name|mock
argument_list|(
name|WAL
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Random
name|random
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|randomBytes
init|=
operator|new
name|byte
index|[
literal|100
index|]
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|family1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family1"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|family2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family2"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Expectations
name|callOnce
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
specifier|public
name|TestBulkLoad
parameter_list|()
throws|throws
name|IOException
block|{
name|callOnce
operator|=
operator|new
name|Expectations
argument_list|()
block|{
block|{
name|oneOf
argument_list|(
name|log
argument_list|)
operator|.
name|append
argument_list|(
name|with
argument_list|(
name|any
argument_list|(
name|HTableDescriptor
operator|.
name|class
argument_list|)
argument_list|)
argument_list|,
name|with
argument_list|(
name|any
argument_list|(
name|HRegionInfo
operator|.
name|class
argument_list|)
argument_list|)
argument_list|,
name|with
argument_list|(
name|any
argument_list|(
name|WALKey
operator|.
name|class
argument_list|)
argument_list|)
argument_list|,
name|with
argument_list|(
name|bulkLogWalEditType
argument_list|(
name|WALEdit
operator|.
name|BULK_LOAD
argument_list|)
argument_list|)
argument_list|,
name|with
argument_list|(
name|any
argument_list|(
name|AtomicLong
operator|.
name|class
argument_list|)
argument_list|)
argument_list|,
name|with
argument_list|(
name|any
argument_list|(
name|boolean
operator|.
name|class
argument_list|)
argument_list|)
argument_list|,
name|with
argument_list|(
name|any
argument_list|(
name|List
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|will
argument_list|(
name|returnValue
argument_list|(
literal|0l
argument_list|)
argument_list|)
expr_stmt|;
name|oneOf
argument_list|(
name|log
argument_list|)
operator|.
name|sync
argument_list|(
name|with
argument_list|(
name|any
argument_list|(
name|long
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|before
parameter_list|()
throws|throws
name|IOException
block|{
name|random
operator|.
name|nextBytes
argument_list|(
name|randomBytes
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|verifyBulkLoadEvent
parameter_list|()
throws|throws
name|IOException
block|{
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"test"
argument_list|,
literal|"test"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
argument_list|>
name|familyPaths
init|=
name|withFamilyPathsFor
argument_list|(
name|family1
argument_list|)
decl_stmt|;
name|byte
index|[]
name|familyName
init|=
name|familyPaths
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getFirst
argument_list|()
decl_stmt|;
name|String
name|storeFileName
init|=
name|familyPaths
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getSecond
argument_list|()
decl_stmt|;
name|storeFileName
operator|=
operator|(
operator|new
name|Path
argument_list|(
name|storeFileName
argument_list|)
operator|)
operator|.
name|getName
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|storeFileNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|storeFileNames
operator|.
name|add
argument_list|(
name|storeFileName
argument_list|)
expr_stmt|;
specifier|final
name|Matcher
argument_list|<
name|WALEdit
argument_list|>
name|bulkEventMatcher
init|=
name|bulkLogWalEdit
argument_list|(
name|WALEdit
operator|.
name|BULK_LOAD
argument_list|,
name|tableName
operator|.
name|toBytes
argument_list|()
argument_list|,
name|familyName
argument_list|,
name|storeFileNames
argument_list|)
decl_stmt|;
name|Expectations
name|expection
init|=
operator|new
name|Expectations
argument_list|()
block|{
block|{
name|oneOf
argument_list|(
name|log
argument_list|)
operator|.
name|append
argument_list|(
name|with
argument_list|(
name|any
argument_list|(
name|HTableDescriptor
operator|.
name|class
argument_list|)
argument_list|)
argument_list|,
name|with
argument_list|(
name|any
argument_list|(
name|HRegionInfo
operator|.
name|class
argument_list|)
argument_list|)
argument_list|,
name|with
argument_list|(
name|any
argument_list|(
name|WALKey
operator|.
name|class
argument_list|)
argument_list|)
argument_list|,
name|with
argument_list|(
name|bulkEventMatcher
argument_list|)
argument_list|,
name|with
argument_list|(
name|any
argument_list|(
name|AtomicLong
operator|.
name|class
argument_list|)
argument_list|)
argument_list|,
name|with
argument_list|(
name|any
argument_list|(
name|boolean
operator|.
name|class
argument_list|)
argument_list|)
argument_list|,
name|with
argument_list|(
name|any
argument_list|(
name|List
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|will
argument_list|(
name|returnValue
argument_list|(
literal|0l
argument_list|)
argument_list|)
expr_stmt|;
name|oneOf
argument_list|(
name|log
argument_list|)
operator|.
name|sync
argument_list|(
name|with
argument_list|(
name|any
argument_list|(
name|long
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|context
operator|.
name|checking
argument_list|(
name|expection
argument_list|)
expr_stmt|;
name|testRegionWithFamiliesAndSpecifiedTableName
argument_list|(
name|tableName
argument_list|,
name|family1
argument_list|)
operator|.
name|bulkLoadHFiles
argument_list|(
name|familyPaths
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|bulkHLogShouldThrowNoErrorAndWriteMarkerWithBlankInput
parameter_list|()
throws|throws
name|IOException
block|{
name|testRegionWithFamilies
argument_list|(
name|family1
argument_list|)
operator|.
name|bulkLoadHFiles
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|()
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|shouldBulkLoadSingleFamilyHLog
parameter_list|()
throws|throws
name|IOException
block|{
name|context
operator|.
name|checking
argument_list|(
name|callOnce
argument_list|)
expr_stmt|;
name|testRegionWithFamilies
argument_list|(
name|family1
argument_list|)
operator|.
name|bulkLoadHFiles
argument_list|(
name|withFamilyPathsFor
argument_list|(
name|family1
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|shouldBulkLoadManyFamilyHLog
parameter_list|()
throws|throws
name|IOException
block|{
name|context
operator|.
name|checking
argument_list|(
name|callOnce
argument_list|)
expr_stmt|;
name|testRegionWithFamilies
argument_list|(
name|family1
argument_list|,
name|family2
argument_list|)
operator|.
name|bulkLoadHFiles
argument_list|(
name|withFamilyPathsFor
argument_list|(
name|family1
argument_list|,
name|family2
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|shouldBulkLoadManyFamilyHLogEvenWhenTableNameNamespaceSpecified
parameter_list|()
throws|throws
name|IOException
block|{
name|context
operator|.
name|checking
argument_list|(
name|callOnce
argument_list|)
expr_stmt|;
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"test"
argument_list|,
literal|"test"
argument_list|)
decl_stmt|;
name|testRegionWithFamiliesAndSpecifiedTableName
argument_list|(
name|tableName
argument_list|,
name|family1
argument_list|,
name|family2
argument_list|)
operator|.
name|bulkLoadHFiles
argument_list|(
name|withFamilyPathsFor
argument_list|(
name|family1
argument_list|,
name|family2
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|DoNotRetryIOException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|shouldCrashIfBulkLoadFamiliesNotInTable
parameter_list|()
throws|throws
name|IOException
block|{
name|testRegionWithFamilies
argument_list|(
name|family1
argument_list|)
operator|.
name|bulkLoadHFiles
argument_list|(
name|withFamilyPathsFor
argument_list|(
name|family1
argument_list|,
name|family2
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|DoNotRetryIOException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|bulkHLogShouldThrowErrorWhenFamilySpecifiedAndHFileExistsButNotInTableDescriptor
parameter_list|()
throws|throws
name|IOException
block|{
name|testRegionWithFamilies
argument_list|()
operator|.
name|bulkLoadHFiles
argument_list|(
name|withFamilyPathsFor
argument_list|(
name|family1
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|DoNotRetryIOException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|shouldThrowErrorIfBadFamilySpecifiedAsFamilyPath
parameter_list|()
throws|throws
name|IOException
block|{
name|testRegionWithFamilies
argument_list|()
operator|.
name|bulkLoadHFiles
argument_list|(
name|asList
argument_list|(
name|withInvalidColumnFamilyButProperHFileLocation
argument_list|(
name|family1
argument_list|)
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|FileNotFoundException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|shouldThrowErrorIfHFileDoesNotExist
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
argument_list|>
name|list
init|=
name|asList
argument_list|(
name|withMissingHFileForFamily
argument_list|(
name|family1
argument_list|)
argument_list|)
decl_stmt|;
name|testRegionWithFamilies
argument_list|(
name|family1
argument_list|)
operator|.
name|bulkLoadHFiles
argument_list|(
name|list
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
name|withMissingHFileForFamily
parameter_list|(
name|byte
index|[]
name|family
parameter_list|)
block|{
return|return
operator|new
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
argument_list|(
name|family
argument_list|,
literal|"/tmp/does_not_exist"
argument_list|)
return|;
block|}
specifier|private
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
name|withInvalidColumnFamilyButProperHFileLocation
parameter_list|(
name|byte
index|[]
name|family
parameter_list|)
throws|throws
name|IOException
block|{
name|createHFileForFamilies
argument_list|(
name|family
argument_list|)
expr_stmt|;
return|return
operator|new
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|0x00
block|,
literal|0x01
block|,
literal|0x02
block|}
argument_list|,
literal|"/tmp/does_not_exist"
argument_list|)
return|;
block|}
specifier|private
name|HRegion
name|testRegionWithFamiliesAndSpecifiedTableName
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
modifier|...
name|families
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegionInfo
name|hRegionInfo
init|=
operator|new
name|HRegionInfo
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|hTableDescriptor
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|family
range|:
name|families
control|)
block|{
name|hTableDescriptor
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// TODO We need a way to do this without creating files
return|return
name|HRegion
operator|.
name|createHRegion
argument_list|(
name|hRegionInfo
argument_list|,
operator|new
name|Path
argument_list|(
name|testFolder
operator|.
name|newFolder
argument_list|()
operator|.
name|toURI
argument_list|()
argument_list|)
argument_list|,
name|conf
argument_list|,
name|hTableDescriptor
argument_list|,
name|log
argument_list|)
return|;
block|}
specifier|private
name|HRegion
name|testRegionWithFamilies
parameter_list|(
name|byte
index|[]
modifier|...
name|families
parameter_list|)
throws|throws
name|IOException
block|{
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
return|return
name|testRegionWithFamiliesAndSpecifiedTableName
argument_list|(
name|tableName
argument_list|,
name|families
argument_list|)
return|;
block|}
specifier|private
name|List
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
argument_list|>
name|getBlankFamilyPaths
parameter_list|()
block|{
return|return
operator|new
name|ArrayList
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|()
return|;
block|}
specifier|private
name|List
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
argument_list|>
name|withFamilyPathsFor
parameter_list|(
name|byte
index|[]
modifier|...
name|families
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
argument_list|>
name|familyPaths
init|=
name|getBlankFamilyPaths
argument_list|()
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|family
range|:
name|families
control|)
block|{
name|familyPaths
operator|.
name|add
argument_list|(
operator|new
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
argument_list|(
name|family
argument_list|,
name|createHFileForFamilies
argument_list|(
name|family
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|familyPaths
return|;
block|}
specifier|private
name|String
name|createHFileForFamilies
parameter_list|(
name|byte
index|[]
name|family
parameter_list|)
throws|throws
name|IOException
block|{
name|HFile
operator|.
name|WriterFactory
name|hFileFactory
init|=
name|HFile
operator|.
name|getWriterFactoryNoCache
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// TODO We need a way to do this without creating files
name|File
name|hFileLocation
init|=
name|testFolder
operator|.
name|newFile
argument_list|()
decl_stmt|;
name|FSDataOutputStream
name|out
init|=
operator|new
name|FSDataOutputStream
argument_list|(
operator|new
name|FileOutputStream
argument_list|(
name|hFileLocation
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|hFileFactory
operator|.
name|withOutputStream
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|hFileFactory
operator|.
name|withFileContext
argument_list|(
operator|new
name|HFileContext
argument_list|()
argument_list|)
expr_stmt|;
name|HFile
operator|.
name|Writer
name|writer
init|=
name|hFileFactory
operator|.
name|create
argument_list|()
decl_stmt|;
try|try
block|{
name|writer
operator|.
name|append
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|CellUtil
operator|.
name|createCell
argument_list|(
name|randomBytes
argument_list|,
name|family
argument_list|,
name|randomBytes
argument_list|,
literal|0l
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
operator|.
name|getCode
argument_list|()
argument_list|,
name|randomBytes
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
name|hFileLocation
operator|.
name|getAbsoluteFile
argument_list|()
operator|.
name|getAbsolutePath
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|Matcher
argument_list|<
name|WALEdit
argument_list|>
name|bulkLogWalEditType
parameter_list|(
name|byte
index|[]
name|typeBytes
parameter_list|)
block|{
return|return
operator|new
name|WalMatcher
argument_list|(
name|typeBytes
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|Matcher
argument_list|<
name|WALEdit
argument_list|>
name|bulkLogWalEdit
parameter_list|(
name|byte
index|[]
name|typeBytes
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
name|familyName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|storeFileNames
parameter_list|)
block|{
return|return
operator|new
name|WalMatcher
argument_list|(
name|typeBytes
argument_list|,
name|tableName
argument_list|,
name|familyName
argument_list|,
name|storeFileNames
argument_list|)
return|;
block|}
specifier|private
specifier|static
class|class
name|WalMatcher
extends|extends
name|TypeSafeMatcher
argument_list|<
name|WALEdit
argument_list|>
block|{
specifier|private
specifier|final
name|byte
index|[]
name|typeBytes
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|familyName
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|storeFileNames
decl_stmt|;
specifier|public
name|WalMatcher
parameter_list|(
name|byte
index|[]
name|typeBytes
parameter_list|)
block|{
name|this
argument_list|(
name|typeBytes
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|WalMatcher
parameter_list|(
name|byte
index|[]
name|typeBytes
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
name|familyName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|storeFileNames
parameter_list|)
block|{
name|this
operator|.
name|typeBytes
operator|=
name|typeBytes
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|familyName
operator|=
name|familyName
expr_stmt|;
name|this
operator|.
name|storeFileNames
operator|=
name|storeFileNames
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|matchesSafely
parameter_list|(
name|WALEdit
name|item
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|Arrays
operator|.
name|equals
argument_list|(
name|CellUtil
operator|.
name|cloneQualifier
argument_list|(
name|item
operator|.
name|getCells
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|,
name|typeBytes
argument_list|)
argument_list|)
expr_stmt|;
name|BulkLoadDescriptor
name|desc
decl_stmt|;
try|try
block|{
name|desc
operator|=
name|WALEdit
operator|.
name|getBulkLoadDescriptor
argument_list|(
name|item
operator|.
name|getCells
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
name|assertNotNull
argument_list|(
name|desc
argument_list|)
expr_stmt|;
if|if
condition|(
name|tableName
operator|!=
literal|null
condition|)
block|{
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|ProtobufUtil
operator|.
name|toTableName
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|,
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|storeFileNames
operator|!=
literal|null
condition|)
block|{
name|int
name|index
init|=
literal|0
decl_stmt|;
name|StoreDescriptor
name|store
init|=
name|desc
operator|.
name|getStores
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|store
operator|.
name|getFamilyName
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|,
name|familyName
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|store
operator|.
name|getStoreHomeDir
argument_list|()
argument_list|)
argument_list|,
name|familyName
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|storeFileNames
operator|.
name|size
argument_list|()
argument_list|,
name|store
operator|.
name|getStoreFileCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|describeTo
parameter_list|(
name|Description
name|description
parameter_list|)
block|{      }
block|}
block|}
end_class

end_unit

