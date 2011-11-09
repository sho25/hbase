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
name|migration
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
name|junit
operator|.
name|framework
operator|.
name|Assert
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
name|*
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
name|MetaMigrationRemovingHTD
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
comment|/**  * Migration tests that do not need spin up of a cluster.  * @deprecated Remove after we release 0.92  */
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
name|TestMigrationFrom090To092
block|{
annotation|@
name|Test
specifier|public
name|void
name|testMigrateHRegionInfoFromVersion0toVersion1
parameter_list|()
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|htd
init|=
name|getHTableDescriptor
argument_list|(
literal|"testMigrateHRegionInfoFromVersion0toVersion1"
argument_list|)
decl_stmt|;
name|HRegionInfo090x
name|ninety
init|=
operator|new
name|HRegionInfo090x
argument_list|(
name|htd
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|Writables
operator|.
name|getBytes
argument_list|(
name|ninety
argument_list|)
decl_stmt|;
comment|// Now deserialize into an HRegionInfo
name|HRegionInfo
name|hri
init|=
name|Writables
operator|.
name|getHRegionInfo
argument_list|(
name|bytes
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|hri
operator|.
name|getTableNameAsString
argument_list|()
argument_list|,
name|ninety
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getNameAsString
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|HRegionInfo
operator|.
name|VERSION
argument_list|,
name|hri
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|HTableDescriptor
name|getHTableDescriptor
parameter_list|(
specifier|final
name|String
name|name
parameter_list|)
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|name
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
return|return
name|htd
return|;
block|}
block|}
end_class

end_unit

