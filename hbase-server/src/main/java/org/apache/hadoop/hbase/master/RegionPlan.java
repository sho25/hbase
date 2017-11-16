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
operator|.
name|master
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * Stores the plan for the move of an individual region.  *  * Contains info for the region being moved, info for the server the region  * should be moved from, and info for the server the region should be moved  * to.  *  * The comparable implementation of this class compares only the region  * information and not the source/dest server info.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
literal|"Coprocessors"
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
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
name|RegionInfo
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
specifier|public
specifier|static
class|class
name|RegionPlanComparator
implements|implements
name|Comparator
argument_list|<
name|RegionPlan
argument_list|>
implements|,
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|4213207330485734853L
decl_stmt|;
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|RegionPlan
name|l
parameter_list|,
name|RegionPlan
name|r
parameter_list|)
block|{
return|return
name|RegionPlan
operator|.
name|compareTo
argument_list|(
name|l
argument_list|,
name|r
argument_list|)
return|;
block|}
block|}
comment|/**    * Instantiate a plan for a region move, moving the specified region from    * the specified source server to the specified destination server.    *    * Destination server can be instantiated as null and later set    * with {@link #setDestination(ServerName)}.    *    * @param hri region to be moved    * @param source regionserver region should be moved from    * @param dest regionserver region should be moved to    */
specifier|public
name|RegionPlan
parameter_list|(
specifier|final
name|RegionInfo
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
name|RegionInfo
name|getRegionInfo
parameter_list|()
block|{
return|return
name|this
operator|.
name|hri
return|;
block|}
comment|/**    * Compare the region info.    * @param other region plan you are comparing against    */
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|RegionPlan
name|other
parameter_list|)
block|{
return|return
name|compareTo
argument_list|(
name|this
argument_list|,
name|other
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|int
name|compareTo
parameter_list|(
name|RegionPlan
name|left
parameter_list|,
name|RegionPlan
name|right
parameter_list|)
block|{
name|int
name|result
init|=
name|compareServerName
argument_list|(
name|left
operator|.
name|source
argument_list|,
name|right
operator|.
name|source
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|0
condition|)
block|{
return|return
name|result
return|;
block|}
if|if
condition|(
name|left
operator|.
name|hri
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|right
operator|.
name|hri
operator|!=
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
block|}
elseif|else
if|if
condition|(
name|right
operator|.
name|hri
operator|==
literal|null
condition|)
block|{
return|return
operator|+
literal|1
return|;
block|}
else|else
block|{
name|result
operator|=
name|RegionInfo
operator|.
name|COMPARATOR
operator|.
name|compare
argument_list|(
name|left
operator|.
name|hri
argument_list|,
name|right
operator|.
name|hri
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|result
operator|!=
literal|0
condition|)
block|{
return|return
name|result
return|;
block|}
return|return
name|compareServerName
argument_list|(
name|left
operator|.
name|dest
argument_list|,
name|right
operator|.
name|dest
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|int
name|compareServerName
parameter_list|(
name|ServerName
name|left
parameter_list|,
name|ServerName
name|right
parameter_list|)
block|{
if|if
condition|(
name|left
operator|==
literal|null
condition|)
block|{
return|return
name|right
operator|==
literal|null
condition|?
literal|0
else|:
operator|-
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|right
operator|==
literal|null
condition|)
block|{
return|return
operator|+
literal|1
return|;
block|}
return|return
name|left
operator|.
name|compareTo
argument_list|(
name|right
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
specifier|final
name|int
name|prime
init|=
literal|31
decl_stmt|;
name|int
name|result
init|=
literal|1
decl_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
operator|(
operator|(
name|dest
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|dest
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
operator|(
operator|(
name|hri
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|hri
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
operator|(
operator|(
name|source
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|source
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|getClass
argument_list|()
operator|!=
name|obj
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|RegionPlan
name|other
init|=
operator|(
name|RegionPlan
operator|)
name|obj
decl_stmt|;
if|if
condition|(
name|dest
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|other
operator|.
name|dest
operator|!=
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|dest
operator|.
name|equals
argument_list|(
name|other
operator|.
name|dest
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|hri
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|other
operator|.
name|hri
operator|!=
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|hri
operator|.
name|equals
argument_list|(
name|other
operator|.
name|hri
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|source
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|other
operator|.
name|source
operator|!=
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|source
operator|.
name|equals
argument_list|(
name|other
operator|.
name|source
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
literal|", source="
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
literal|", destination="
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

