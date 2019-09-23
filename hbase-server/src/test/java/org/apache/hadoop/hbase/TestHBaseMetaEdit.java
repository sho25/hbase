begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|RegionInfoBuilder
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
name|io
operator|.
name|encoding
operator|.
name|DataBlockEncoding
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
name|After
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
name|TestName
import|;
end_import

begin_comment
comment|/**  * Test being able to edit hbase:meta.  */
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
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestHBaseMetaEdit
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
name|TestHBaseMetaEdit
operator|.
name|class
argument_list|)
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
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|before
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|after
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * Set versions, set HBASE-16213 indexed block encoding, and add a column family.    * Verify they are all in place by looking at TableDescriptor AND by checking    * what the RegionServer sees after opening Region.    */
annotation|@
name|Test
specifier|public
name|void
name|testEditMeta
parameter_list|()
throws|throws
name|IOException
block|{
name|Admin
name|admin
init|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|admin
operator|.
name|tableExists
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|admin
operator|.
name|isTableDisabled
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
argument_list|)
expr_stmt|;
name|TableDescriptor
name|descriptor
init|=
name|admin
operator|.
name|getDescriptor
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
name|ColumnFamilyDescriptor
name|cfd
init|=
name|descriptor
operator|.
name|getColumnFamily
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
decl_stmt|;
name|byte
index|[]
name|extraColumnFamilyName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"xtra"
argument_list|)
decl_stmt|;
name|ColumnFamilyDescriptor
name|newCfd
init|=
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|extraColumnFamilyName
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|int
name|oldVersions
init|=
name|cfd
operator|.
name|getMaxVersions
argument_list|()
decl_stmt|;
comment|// Add '1' to current versions count.
name|cfd
operator|=
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|cfd
argument_list|)
operator|.
name|setMaxVersions
argument_list|(
name|oldVersions
operator|+
literal|1
argument_list|)
operator|.
name|setConfiguration
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|DATA_BLOCK_ENCODING
argument_list|,
name|DataBlockEncoding
operator|.
name|ROW_INDEX_V1
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|admin
operator|.
name|modifyColumnFamily
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|,
name|cfd
argument_list|)
expr_stmt|;
name|admin
operator|.
name|addColumnFamily
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|,
name|newCfd
argument_list|)
expr_stmt|;
name|descriptor
operator|=
name|admin
operator|.
name|getDescriptor
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
comment|// Assert new max versions is == old versions plus 1.
name|assertEquals
argument_list|(
name|oldVersions
operator|+
literal|1
argument_list|,
name|descriptor
operator|.
name|getColumnFamily
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
operator|.
name|getMaxVersions
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|enableTable
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
name|descriptor
operator|=
name|admin
operator|.
name|getDescriptor
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
comment|// Assert new max versions is == old versions plus 1.
name|assertEquals
argument_list|(
name|oldVersions
operator|+
literal|1
argument_list|,
name|descriptor
operator|.
name|getColumnFamily
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
operator|.
name|getMaxVersions
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|descriptor
operator|.
name|getColumnFamily
argument_list|(
name|newCfd
operator|.
name|getName
argument_list|()
argument_list|)
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|String
name|encoding
init|=
name|descriptor
operator|.
name|getColumnFamily
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|DATA_BLOCK_ENCODING
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|encoding
argument_list|,
name|DataBlockEncoding
operator|.
name|ROW_INDEX_V1
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Region
name|r
init|=
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegion
argument_list|(
name|RegionInfoBuilder
operator|.
name|FIRST_META_REGIONINFO
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|oldVersions
operator|+
literal|1
argument_list|,
name|r
operator|.
name|getStore
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
operator|.
name|getColumnFamilyDescriptor
argument_list|()
operator|.
name|getMaxVersions
argument_list|()
argument_list|)
expr_stmt|;
name|encoding
operator|=
name|r
operator|.
name|getStore
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
operator|.
name|getColumnFamilyDescriptor
argument_list|()
operator|.
name|getConfigurationValue
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|DATA_BLOCK_ENCODING
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|encoding
argument_list|,
name|DataBlockEncoding
operator|.
name|ROW_INDEX_V1
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|r
operator|.
name|getStore
argument_list|(
name|extraColumnFamilyName
argument_list|)
operator|!=
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

