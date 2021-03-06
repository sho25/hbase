begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
package|;
end_package

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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|RegionStatesCount
block|{
specifier|private
name|int
name|openRegions
decl_stmt|;
specifier|private
name|int
name|splitRegions
decl_stmt|;
specifier|private
name|int
name|closedRegions
decl_stmt|;
specifier|private
name|int
name|regionsInTransition
decl_stmt|;
specifier|private
name|int
name|totalRegions
decl_stmt|;
specifier|private
name|RegionStatesCount
parameter_list|()
block|{   }
specifier|public
name|int
name|getClosedRegions
parameter_list|()
block|{
return|return
name|closedRegions
return|;
block|}
specifier|public
name|int
name|getOpenRegions
parameter_list|()
block|{
return|return
name|openRegions
return|;
block|}
specifier|public
name|int
name|getSplitRegions
parameter_list|()
block|{
return|return
name|splitRegions
return|;
block|}
specifier|public
name|int
name|getRegionsInTransition
parameter_list|()
block|{
return|return
name|regionsInTransition
return|;
block|}
specifier|public
name|int
name|getTotalRegions
parameter_list|()
block|{
return|return
name|totalRegions
return|;
block|}
specifier|private
name|void
name|setClosedRegions
parameter_list|(
name|int
name|closedRegions
parameter_list|)
block|{
name|this
operator|.
name|closedRegions
operator|=
name|closedRegions
expr_stmt|;
block|}
specifier|private
name|void
name|setOpenRegions
parameter_list|(
name|int
name|openRegions
parameter_list|)
block|{
name|this
operator|.
name|openRegions
operator|=
name|openRegions
expr_stmt|;
block|}
specifier|private
name|void
name|setSplitRegions
parameter_list|(
name|int
name|splitRegions
parameter_list|)
block|{
name|this
operator|.
name|splitRegions
operator|=
name|splitRegions
expr_stmt|;
block|}
specifier|private
name|void
name|setRegionsInTransition
parameter_list|(
name|int
name|regionsInTransition
parameter_list|)
block|{
name|this
operator|.
name|regionsInTransition
operator|=
name|regionsInTransition
expr_stmt|;
block|}
specifier|private
name|void
name|setTotalRegions
parameter_list|(
name|int
name|totalRegions
parameter_list|)
block|{
name|this
operator|.
name|totalRegions
operator|=
name|totalRegions
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|RegionStatesCountBuilder
block|{
specifier|private
name|int
name|openRegions
decl_stmt|;
specifier|private
name|int
name|splitRegions
decl_stmt|;
specifier|private
name|int
name|closedRegions
decl_stmt|;
specifier|private
name|int
name|regionsInTransition
decl_stmt|;
specifier|private
name|int
name|totalRegions
decl_stmt|;
specifier|public
name|RegionStatesCountBuilder
name|setOpenRegions
parameter_list|(
name|int
name|openRegions
parameter_list|)
block|{
name|this
operator|.
name|openRegions
operator|=
name|openRegions
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionStatesCountBuilder
name|setSplitRegions
parameter_list|(
name|int
name|splitRegions
parameter_list|)
block|{
name|this
operator|.
name|splitRegions
operator|=
name|splitRegions
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionStatesCountBuilder
name|setClosedRegions
parameter_list|(
name|int
name|closedRegions
parameter_list|)
block|{
name|this
operator|.
name|closedRegions
operator|=
name|closedRegions
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionStatesCountBuilder
name|setRegionsInTransition
parameter_list|(
name|int
name|regionsInTransition
parameter_list|)
block|{
name|this
operator|.
name|regionsInTransition
operator|=
name|regionsInTransition
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionStatesCountBuilder
name|setTotalRegions
parameter_list|(
name|int
name|totalRegions
parameter_list|)
block|{
name|this
operator|.
name|totalRegions
operator|=
name|totalRegions
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|RegionStatesCount
name|build
parameter_list|()
block|{
name|RegionStatesCount
name|regionStatesCount
init|=
operator|new
name|RegionStatesCount
argument_list|()
decl_stmt|;
name|regionStatesCount
operator|.
name|setOpenRegions
argument_list|(
name|openRegions
argument_list|)
expr_stmt|;
name|regionStatesCount
operator|.
name|setClosedRegions
argument_list|(
name|closedRegions
argument_list|)
expr_stmt|;
name|regionStatesCount
operator|.
name|setRegionsInTransition
argument_list|(
name|regionsInTransition
argument_list|)
expr_stmt|;
name|regionStatesCount
operator|.
name|setSplitRegions
argument_list|(
name|splitRegions
argument_list|)
expr_stmt|;
name|regionStatesCount
operator|.
name|setTotalRegions
argument_list|(
name|totalRegions
argument_list|)
expr_stmt|;
return|return
name|regionStatesCount
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
specifier|final
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"RegionStatesCount{"
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"openRegions="
argument_list|)
operator|.
name|append
argument_list|(
name|openRegions
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", splitRegions="
argument_list|)
operator|.
name|append
argument_list|(
name|splitRegions
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", closedRegions="
argument_list|)
operator|.
name|append
argument_list|(
name|closedRegions
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", regionsInTransition="
argument_list|)
operator|.
name|append
argument_list|(
name|regionsInTransition
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", totalRegions="
argument_list|)
operator|.
name|append
argument_list|(
name|totalRegions
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|'}'
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|RegionStatesCount
name|that
init|=
operator|(
name|RegionStatesCount
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|openRegions
operator|!=
name|that
operator|.
name|openRegions
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|splitRegions
operator|!=
name|that
operator|.
name|splitRegions
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|closedRegions
operator|!=
name|that
operator|.
name|closedRegions
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|regionsInTransition
operator|!=
name|that
operator|.
name|regionsInTransition
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|totalRegions
operator|==
name|that
operator|.
name|totalRegions
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|openRegions
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|splitRegions
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|closedRegions
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|regionsInTransition
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|totalRegions
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

