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
name|util
operator|.
name|hbck
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
name|Collection
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
name|HBaseFsck
operator|.
name|HbckInfo
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
name|HBaseFsck
operator|.
name|TableInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Simple implementation of TableIntegrityErrorHandler. Can be used as a base  * class.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|abstract
specifier|public
class|class
name|TableIntegrityErrorHandlerImpl
implements|implements
name|TableIntegrityErrorHandler
block|{
name|TableInfo
name|ti
decl_stmt|;
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|TableInfo
name|getTableInfo
parameter_list|()
block|{
return|return
name|ti
return|;
block|}
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|void
name|setTableInfo
parameter_list|(
name|TableInfo
name|ti2
parameter_list|)
block|{
name|this
operator|.
name|ti
operator|=
name|ti2
expr_stmt|;
block|}
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|void
name|handleRegionStartKeyNotEmpty
parameter_list|(
name|HbckInfo
name|hi
parameter_list|)
throws|throws
name|IOException
block|{   }
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|void
name|handleRegionEndKeyNotEmpty
parameter_list|(
name|byte
index|[]
name|curEndKey
parameter_list|)
throws|throws
name|IOException
block|{   }
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|void
name|handleDegenerateRegion
parameter_list|(
name|HbckInfo
name|hi
parameter_list|)
throws|throws
name|IOException
block|{   }
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|void
name|handleDuplicateStartKeys
parameter_list|(
name|HbckInfo
name|hi1
parameter_list|,
name|HbckInfo
name|hi2
parameter_list|)
throws|throws
name|IOException
block|{   }
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|void
name|handleOverlapInRegionChain
parameter_list|(
name|HbckInfo
name|hi1
parameter_list|,
name|HbckInfo
name|hi2
parameter_list|)
throws|throws
name|IOException
block|{   }
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|void
name|handleHoleInRegionChain
parameter_list|(
name|byte
index|[]
name|holeStart
parameter_list|,
name|byte
index|[]
name|holeEnd
parameter_list|)
throws|throws
name|IOException
block|{   }
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|void
name|handleOverlapGroup
parameter_list|(
name|Collection
argument_list|<
name|HbckInfo
argument_list|>
name|overlap
parameter_list|)
throws|throws
name|IOException
block|{   }
block|}
end_class

end_unit

