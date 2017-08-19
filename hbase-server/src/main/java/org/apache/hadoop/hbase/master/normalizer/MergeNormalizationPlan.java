begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|normalizer
package|;
end_package

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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
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
name|master
operator|.
name|normalizer
operator|.
name|NormalizationPlan
operator|.
name|PlanType
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

begin_comment
comment|/**  * Normalization plan to merge regions (smallest region in the table with its smallest neighbor).  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MergeNormalizationPlan
implements|implements
name|NormalizationPlan
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
name|MergeNormalizationPlan
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HRegionInfo
name|firstRegion
decl_stmt|;
specifier|private
specifier|final
name|HRegionInfo
name|secondRegion
decl_stmt|;
specifier|public
name|MergeNormalizationPlan
parameter_list|(
name|HRegionInfo
name|firstRegion
parameter_list|,
name|HRegionInfo
name|secondRegion
parameter_list|)
block|{
name|this
operator|.
name|firstRegion
operator|=
name|firstRegion
expr_stmt|;
name|this
operator|.
name|secondRegion
operator|=
name|secondRegion
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|PlanType
name|getType
parameter_list|()
block|{
return|return
name|PlanType
operator|.
name|MERGE
return|;
block|}
name|HRegionInfo
name|getFirstRegion
parameter_list|()
block|{
return|return
name|firstRegion
return|;
block|}
name|HRegionInfo
name|getSecondRegion
parameter_list|()
block|{
return|return
name|secondRegion
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"MergeNormalizationPlan{"
operator|+
literal|"firstRegion="
operator|+
name|firstRegion
operator|+
literal|", secondRegion="
operator|+
name|secondRegion
operator|+
literal|'}'
return|;
block|}
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|void
name|execute
parameter_list|(
name|Admin
name|admin
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Executing merging normalization plan: "
operator|+
name|this
argument_list|)
expr_stmt|;
try|try
block|{
name|admin
operator|.
name|mergeRegionsAsync
argument_list|(
name|firstRegion
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|secondRegion
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error during region merge: "
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

