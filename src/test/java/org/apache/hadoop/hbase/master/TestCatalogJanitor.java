begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|assertNull
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
name|NotAllMetaRegionsOnlineException
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
name|Server
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
name|catalog
operator|.
name|CatalogTracker
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
name|executor
operator|.
name|ExecutorService
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
name|Reference
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
name|ipc
operator|.
name|HRegionInterface
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
name|Store
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
name|Writables
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
name|zookeeper
operator|.
name|ZooKeeperWatcher
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
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_class
specifier|public
class|class
name|TestCatalogJanitor
block|{
comment|/**    * Pseudo server for below tests.    */
class|class
name|MockServer
implements|implements
name|Server
block|{
specifier|private
specifier|final
name|Configuration
name|c
decl_stmt|;
specifier|private
specifier|final
name|CatalogTracker
name|ct
decl_stmt|;
name|MockServer
parameter_list|(
specifier|final
name|HBaseTestingUtility
name|htu
parameter_list|)
throws|throws
name|NotAllMetaRegionsOnlineException
throws|,
name|IOException
block|{
name|this
operator|.
name|c
operator|=
name|htu
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
comment|// Set hbase.rootdir into test dir.
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|this
operator|.
name|c
argument_list|)
decl_stmt|;
name|Path
name|rootdir
init|=
name|fs
operator|.
name|makeQualified
argument_list|(
name|HBaseTestingUtility
operator|.
name|getTestDir
argument_list|(
name|HConstants
operator|.
name|HBASE_DIR
argument_list|)
argument_list|)
decl_stmt|;
name|this
operator|.
name|c
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HBASE_DIR
argument_list|,
name|rootdir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|ct
operator|=
name|Mockito
operator|.
name|mock
argument_list|(
name|CatalogTracker
operator|.
name|class
argument_list|)
expr_stmt|;
name|HRegionInterface
name|hri
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HRegionInterface
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|ct
operator|.
name|waitForMetaServerConnectionDefault
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|hri
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|CatalogTracker
name|getCatalogTracker
parameter_list|()
block|{
return|return
name|this
operator|.
name|ct
return|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|this
operator|.
name|c
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getServerName
parameter_list|()
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ZooKeeperWatcher
name|getZooKeeper
parameter_list|()
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
comment|// TODO Auto-generated method stub
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
comment|// TODO Auto-generated method stub
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|why
parameter_list|)
block|{
comment|// TODO Auto-generated method stub
block|}
block|}
comment|/**    * Mock MasterServices for tests below.    */
class|class
name|MockMasterServices
implements|implements
name|MasterServices
block|{
specifier|private
specifier|final
name|MasterFileSystem
name|mfs
decl_stmt|;
name|MockMasterServices
parameter_list|(
specifier|final
name|Server
name|server
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|mfs
operator|=
operator|new
name|MasterFileSystem
argument_list|(
name|server
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|checkTableModifiable
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
comment|// TODO Auto-generated method stub
block|}
annotation|@
name|Override
specifier|public
name|AssignmentManager
name|getAssignmentManager
parameter_list|()
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ExecutorService
name|getExecutorService
parameter_list|()
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|MasterFileSystem
name|getMasterFileSystem
parameter_list|()
block|{
return|return
name|this
operator|.
name|mfs
return|;
block|}
annotation|@
name|Override
specifier|public
name|ServerManager
name|getServerManager
parameter_list|()
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ZooKeeperWatcher
name|getZooKeeperWatcher
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|CatalogTracker
name|getCatalogTracker
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetHRegionInfo
parameter_list|()
throws|throws
name|IOException
block|{
name|assertNull
argument_list|(
name|CatalogJanitor
operator|.
name|getHRegionInfo
argument_list|(
operator|new
name|Result
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|Result
name|r
init|=
operator|new
name|Result
argument_list|(
name|kvs
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|CatalogJanitor
operator|.
name|getHRegionInfo
argument_list|(
name|r
argument_list|)
argument_list|)
expr_stmt|;
name|byte
index|[]
name|f
init|=
name|HConstants
operator|.
name|CATALOG_FAMILY
decl_stmt|;
comment|// Make a key value that doesn't have the expected qualifier.
name|kvs
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
name|f
argument_list|,
name|HConstants
operator|.
name|SERVER_QUALIFIER
argument_list|,
name|f
argument_list|)
argument_list|)
expr_stmt|;
name|r
operator|=
operator|new
name|Result
argument_list|(
name|kvs
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|CatalogJanitor
operator|.
name|getHRegionInfo
argument_list|(
name|r
argument_list|)
argument_list|)
expr_stmt|;
comment|// Make a key that does not have a regioninfo value.
name|kvs
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
name|f
argument_list|,
name|HConstants
operator|.
name|REGIONINFO_QUALIFIER
argument_list|,
name|f
argument_list|)
argument_list|)
expr_stmt|;
name|HRegionInfo
name|hri
init|=
name|CatalogJanitor
operator|.
name|getHRegionInfo
argument_list|(
operator|new
name|Result
argument_list|(
name|kvs
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|hri
operator|==
literal|null
argument_list|)
expr_stmt|;
comment|// OK, give it what it expects
name|kvs
operator|.
name|clear
argument_list|()
expr_stmt|;
name|kvs
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
name|f
argument_list|,
name|HConstants
operator|.
name|REGIONINFO_QUALIFIER
argument_list|,
name|Writables
operator|.
name|getBytes
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|hri
operator|=
name|CatalogJanitor
operator|.
name|getHRegionInfo
argument_list|(
operator|new
name|Result
argument_list|(
name|kvs
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|hri
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|hri
operator|.
name|equals
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCleanParent
parameter_list|()
throws|throws
name|IOException
block|{
name|HBaseTestingUtility
name|htu
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
name|Server
name|server
init|=
operator|new
name|MockServer
argument_list|(
name|htu
argument_list|)
decl_stmt|;
name|MasterServices
name|services
init|=
operator|new
name|MockMasterServices
argument_list|(
name|server
argument_list|)
decl_stmt|;
name|CatalogJanitor
name|janitor
init|=
operator|new
name|CatalogJanitor
argument_list|(
name|server
argument_list|,
name|services
argument_list|)
decl_stmt|;
comment|// Create regions.
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
literal|"table"
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
literal|"family"
argument_list|)
argument_list|)
expr_stmt|;
name|HRegionInfo
name|parent
init|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaa"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"eee"
argument_list|)
argument_list|)
decl_stmt|;
name|HRegionInfo
name|splita
init|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaa"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ccc"
argument_list|)
argument_list|)
decl_stmt|;
name|HRegionInfo
name|splitb
init|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ccc"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"eee"
argument_list|)
argument_list|)
decl_stmt|;
comment|// Test that when both daughter regions are in place, that we do not
comment|// remove the parent.
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|kvs
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|parent
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|SPLITA_QUALIFIER
argument_list|,
name|Writables
operator|.
name|getBytes
argument_list|(
name|splita
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|kvs
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|parent
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|SPLITB_QUALIFIER
argument_list|,
name|Writables
operator|.
name|getBytes
argument_list|(
name|splitb
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Result
name|r
init|=
operator|new
name|Result
argument_list|(
name|kvs
argument_list|)
decl_stmt|;
comment|// Add a reference under splitA directory so we don't clear out the parent.
name|Path
name|rootdir
init|=
name|services
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getRootDir
argument_list|()
decl_stmt|;
name|Path
name|tabledir
init|=
name|HTableDescriptor
operator|.
name|getTableDir
argument_list|(
name|rootdir
argument_list|,
name|htd
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|storedir
init|=
name|Store
operator|.
name|getStoreHomedir
argument_list|(
name|tabledir
argument_list|,
name|splita
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|htd
operator|.
name|getColumnFamilies
argument_list|()
index|[
literal|0
index|]
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|Reference
name|ref
init|=
operator|new
name|Reference
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ccc"
argument_list|)
argument_list|,
name|Reference
operator|.
name|Range
operator|.
name|top
argument_list|)
decl_stmt|;
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
comment|// Reference name has this format: StoreFile#REF_NAME_PARSER
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|storedir
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
name|now
argument_list|)
operator|+
literal|"."
operator|+
name|parent
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|services
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|ref
operator|.
name|write
argument_list|(
name|fs
argument_list|,
name|p
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|janitor
operator|.
name|cleanParent
argument_list|(
name|parent
argument_list|,
name|r
argument_list|)
argument_list|)
expr_stmt|;
comment|// Remove the reference file and try again.
name|assertTrue
argument_list|(
name|fs
operator|.
name|delete
argument_list|(
name|p
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|janitor
operator|.
name|cleanParent
argument_list|(
name|parent
argument_list|,
name|r
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

