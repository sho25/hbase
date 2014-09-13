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
operator|.
name|wal
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
name|hbase
operator|.
name|testclassification
operator|.
name|RegionServerTests
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
comment|/**  * Test that we can create, load, setup our own custom codec  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
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
name|TestCustomWALCellCodec
block|{
specifier|public
specifier|static
class|class
name|CustomWALCellCodec
extends|extends
name|WALCellCodec
block|{
specifier|public
name|Configuration
name|conf
decl_stmt|;
specifier|public
name|CompressionContext
name|context
decl_stmt|;
specifier|public
name|CustomWALCellCodec
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|CompressionContext
name|compression
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|compression
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|context
operator|=
name|compression
expr_stmt|;
block|}
block|}
comment|/**    * Test that a custom {@link WALCellCodec} will be completely setup when it is instantiated via    * {@link WALCellCodec}    * @throws Exception on failure    */
annotation|@
name|Test
specifier|public
name|void
name|testCreatePreparesCodec
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setClass
argument_list|(
name|WALCellCodec
operator|.
name|WAL_CELL_CODEC_CLASS_KEY
argument_list|,
name|CustomWALCellCodec
operator|.
name|class
argument_list|,
name|WALCellCodec
operator|.
name|class
argument_list|)
expr_stmt|;
name|CustomWALCellCodec
name|codec
init|=
operator|(
name|CustomWALCellCodec
operator|)
name|WALCellCodec
operator|.
name|create
argument_list|(
name|conf
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Custom codec didn't get initialized with the right configuration!"
argument_list|,
name|conf
argument_list|,
name|codec
operator|.
name|conf
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Custom codec didn't get initialized with the right compression context!"
argument_list|,
literal|null
argument_list|,
name|codec
operator|.
name|context
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

