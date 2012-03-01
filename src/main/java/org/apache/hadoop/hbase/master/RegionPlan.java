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
name|master
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
name|classification
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
name|ServerName
import|;
end_import

begin_comment
comment|/**  * Stores the plan for the move of an individual region.  *  * Contains info for the region being moved, info for the server the region  * should be moved from, and info for the server the region should be moved  * to.  *  * The comparable implementation of this class compares only the region  * information and not the source/dest server info.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RegionPlan
implements|implements
name|Comparable
argument_list|<
name|RegionPlan
argument_list|>
block|{
specifier|private
specifier|final
name|HRegionInfo
name|hri
decl_stmt|;
specifier|private
specifier|final
name|ServerName
name|source
decl_stmt|;
specifier|private
name|ServerName
name|dest
decl_stmt|;
comment|/**    * Instantiate a plan for a region move, moving the specified region from    * the specified source server to the specified destination server.    *    * Destination server can be instantiated as null and later set    * with {@link #setDestination(ServerName)}.    *    * @param hri region to be moved    * @param source regionserver region should be moved from    * @param dest regionserver region should be moved to    */
specifier|public
name|RegionPlan
parameter_list|(
specifier|final
name|HRegionInfo
name|hri
parameter_list|,
name|ServerName
name|source
parameter_list|,
name|ServerName
name|dest
parameter_list|)
block|{
name|this
operator|.
name|hri
operator|=
name|hri
expr_stmt|;
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
name|this
operator|.
name|dest
operator|=
name|dest
expr_stmt|;
block|}
comment|/**    * Set the destination server for the plan for this region.    */
specifier|public
name|void
name|setDestination
parameter_list|(
name|ServerName
name|dest
parameter_list|)
block|{
name|this
operator|.
name|dest
operator|=
name|dest
expr_stmt|;
block|}
comment|/**    * Get the source server for the plan for this region.    * @return server info for source    */
specifier|public
name|ServerName
name|getSource
parameter_list|()
block|{
return|return
name|source
return|;
block|}
comment|/**    * Get the destination server for the plan for this region.    * @return server info for destination    */
specifier|public
name|ServerName
name|getDestination
parameter_list|()
block|{
return|return
name|dest
return|;
block|}
comment|/**    * Get the encoded region name for the region this plan is for.    * @return Encoded region name    */
specifier|public
name|String
name|getRegionName
parameter_list|()
block|{
return|return
name|this
operator|.
name|hri
operator|.
name|getEncodedName
argument_list|()
return|;
block|}
specifier|public
name|HRegionInfo
name|getRegionInfo
parameter_list|()
block|{
return|return
name|this
operator|.
name|hri
return|;
block|}
comment|/**    * Compare the region info.    * @param o region plan you are comparing against    */
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|RegionPlan
name|o
parameter_list|)
block|{
return|return
name|getRegionName
argument_list|()
operator|.
name|compareTo
argument_list|(
name|o
operator|.
name|getRegionName
argument_list|()
argument_list|)
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
literal|"hri="
operator|+
name|this
operator|.
name|hri
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|", src="
operator|+
operator|(
name|this
operator|.
name|source
operator|==
literal|null
condition|?
literal|""
else|:
name|this
operator|.
name|source
operator|.
name|toString
argument_list|()
operator|)
operator|+
literal|", dest="
operator|+
operator|(
name|this
operator|.
name|dest
operator|==
literal|null
condition|?
literal|""
else|:
name|this
operator|.
name|dest
operator|.
name|toString
argument_list|()
operator|)
return|;
block|}
block|}
end_class

end_unit

