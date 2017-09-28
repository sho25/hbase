begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|coprocessor
operator|.
name|example
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
name|ArrayList
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
name|MiniHBaseCluster
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
name|RegionInfo
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
name|RetriesExhaustedException
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
name|example
operator|.
name|RefreshHFilesClient
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
name|coprocessor
operator|.
name|CoprocessorHost
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
name|master
operator|.
name|MasterFileSystem
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
name|HRegion
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
name|HStore
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
name|Region
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
name|RegionServerServices
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
name|HFileTestUtil
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
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestRefreshHFilesEndpoint
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
name|TestRefreshHFilesEndpoint
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|HTU
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_MASTER
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_RS
init|=
literal|2
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testRefreshRegionHFilesEP"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
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
specifier|static
specifier|final
name|byte
index|[]
name|QUALIFIER
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
specifier|final
name|byte
index|[]
index|[]
name|SPLIT_KEY
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
literal|"30"
argument_list|)
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_ROWS
init|=
literal|5
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HFILE_NAME
init|=
literal|"123abcdef"
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|CONF
init|=
name|HTU
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|MiniHBaseCluster
name|cluster
decl_stmt|;
specifier|private
specifier|static
name|Table
name|table
decl_stmt|;
specifier|public
specifier|static
name|void
name|setUp
parameter_list|(
name|String
name|regionImpl
parameter_list|)
block|{
try|try
block|{
name|CONF
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|REGION_IMPL
argument_list|,
name|regionImpl
argument_list|)
expr_stmt|;
name|CONF
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|CONF
operator|.
name|setStrings
argument_list|(
name|CoprocessorHost
operator|.
name|REGION_COPROCESSOR_CONF_KEY
argument_list|,
name|RefreshHFilesEndpoint
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|cluster
operator|=
name|HTU
operator|.
name|startMiniCluster
argument_list|(
name|NUM_MASTER
argument_list|,
name|NUM_RS
argument_list|)
expr_stmt|;
comment|// Create table
name|table
operator|=
name|HTU
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY
argument_list|,
name|SPLIT_KEY
argument_list|)
expr_stmt|;
comment|// this will create 2 regions spread across slaves
name|HTU
operator|.
name|loadNumericRows
argument_list|(
name|table
argument_list|,
name|FAMILY
argument_list|,
literal|1
argument_list|,
literal|20
argument_list|)
expr_stmt|;
name|HTU
operator|.
name|flush
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Couldn't finish setup"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|HTU
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRefreshRegionHFilesEndpoint
parameter_list|()
throws|throws
name|Exception
block|{
name|setUp
argument_list|(
name|HRegion
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|MasterFileSystem
name|mfs
init|=
name|HTU
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterFileSystem
argument_list|()
decl_stmt|;
name|Path
name|tableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|mfs
operator|.
name|getRootDir
argument_list|()
argument_list|,
name|TABLE_NAME
argument_list|)
decl_stmt|;
for|for
control|(
name|Region
name|region
range|:
name|cluster
operator|.
name|getRegions
argument_list|(
name|TABLE_NAME
argument_list|)
control|)
block|{
name|Path
name|regionDir
init|=
operator|new
name|Path
argument_list|(
name|tableDir
argument_list|,
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|familyDir
init|=
operator|new
name|Path
argument_list|(
name|regionDir
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
decl_stmt|;
name|HFileTestUtil
operator|.
name|createHFile
argument_list|(
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|HTU
operator|.
name|getTestFileSystem
argument_list|()
argument_list|,
operator|new
name|Path
argument_list|(
name|familyDir
argument_list|,
name|HFILE_NAME
argument_list|)
argument_list|,
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"50"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"60"
argument_list|)
argument_list|,
name|NUM_ROWS
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|HTU
operator|.
name|getNumHFiles
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY
argument_list|)
argument_list|)
expr_stmt|;
name|callRefreshRegionHFilesEndPoint
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|HTU
operator|.
name|getNumHFiles
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILY
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|testRefreshRegionHFilesEndpointWithException
parameter_list|()
throws|throws
name|IOException
block|{
name|setUp
argument_list|(
name|HRegionForRefreshHFilesEP
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|callRefreshRegionHFilesEndPoint
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|callRefreshRegionHFilesEndPoint
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|RefreshHFilesClient
name|refreshHFilesClient
init|=
operator|new
name|RefreshHFilesClient
argument_list|(
name|CONF
argument_list|)
decl_stmt|;
name|refreshHFilesClient
operator|.
name|refreshHFiles
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RetriesExhaustedException
name|rex
parameter_list|)
block|{
if|if
condition|(
name|rex
operator|.
name|getCause
argument_list|()
operator|instanceof
name|IOException
condition|)
throw|throw
operator|new
name|IOException
argument_list|()
throw|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|ex
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Couldn't call the RefreshRegionHFilesEndpoint"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|HRegionForRefreshHFilesEP
extends|extends
name|HRegion
block|{
name|HStoreWithFaultyRefreshHFilesAPI
name|store
decl_stmt|;
specifier|public
name|HRegionForRefreshHFilesEP
parameter_list|(
specifier|final
name|Path
name|tableDir
parameter_list|,
specifier|final
name|WAL
name|wal
parameter_list|,
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Configuration
name|confParam
parameter_list|,
specifier|final
name|RegionInfo
name|regionInfo
parameter_list|,
specifier|final
name|TableDescriptor
name|htd
parameter_list|,
specifier|final
name|RegionServerServices
name|rsServices
parameter_list|)
block|{
name|super
argument_list|(
name|tableDir
argument_list|,
name|wal
argument_list|,
name|fs
argument_list|,
name|confParam
argument_list|,
name|regionInfo
argument_list|,
name|htd
argument_list|,
name|rsServices
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|HStore
argument_list|>
name|getStores
parameter_list|()
block|{
name|List
argument_list|<
name|HStore
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|stores
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
comment|/**        * This is used to trigger the custom definition (faulty)        * of refresh HFiles API.        */
try|try
block|{
if|if
condition|(
name|this
operator|.
name|store
operator|==
literal|null
condition|)
block|{
name|store
operator|=
operator|new
name|HStoreWithFaultyRefreshHFilesAPI
argument_list|(
name|this
argument_list|,
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
name|FAMILY
argument_list|)
argument_list|,
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
block|}
name|list
operator|.
name|add
argument_list|(
name|store
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Couldn't instantiate custom store implementation"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
name|list
operator|.
name|addAll
argument_list|(
name|stores
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|list
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|HStoreWithFaultyRefreshHFilesAPI
extends|extends
name|HStore
block|{
specifier|public
name|HStoreWithFaultyRefreshHFilesAPI
parameter_list|(
specifier|final
name|HRegion
name|region
parameter_list|,
specifier|final
name|ColumnFamilyDescriptor
name|family
parameter_list|,
specifier|final
name|Configuration
name|confParam
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|region
argument_list|,
name|family
argument_list|,
name|confParam
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|refreshStoreFiles
parameter_list|()
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|IOException
argument_list|()
throw|;
block|}
block|}
block|}
end_class

end_unit

