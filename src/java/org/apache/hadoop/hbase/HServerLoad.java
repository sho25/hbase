begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|io
operator|.
name|WritableComparable
import|;
end_import

begin_comment
comment|/**  * This class encapsulates metrics for determining the load on a HRegionServer  */
end_comment

begin_class
specifier|public
class|class
name|HServerLoad
implements|implements
name|WritableComparable
block|{
specifier|private
name|int
name|numberOfRequests
decl_stmt|;
comment|// number of requests since last report
specifier|private
name|int
name|numberOfRegions
decl_stmt|;
comment|// number of regions being served
comment|/*    * TODO: Other metrics that might be considered when the master is actually    * doing load balancing instead of merely trying to decide where to assign    * a region:    *<ul>    *<li># of CPUs, heap size (to determine the "class" of machine). For    *       now, we consider them to be homogeneous.</li>    *<li>#requests per region (Map<{String|HRegionInfo}, Integer>)</li>    *<li>#compactions and/or #splits (churn)</li>    *<li>server death rate (maybe there is something wrong with this server)</li>    *</ul>    */
comment|/** default constructior (used by Writable) */
specifier|public
name|HServerLoad
parameter_list|()
block|{}
comment|/**    * Constructor    * @param numberOfRequests    * @param numberOfRegions    */
specifier|public
name|HServerLoad
parameter_list|(
name|int
name|numberOfRequests
parameter_list|,
name|int
name|numberOfRegions
parameter_list|)
block|{
name|this
operator|.
name|numberOfRequests
operator|=
name|numberOfRequests
expr_stmt|;
name|this
operator|.
name|numberOfRegions
operator|=
name|numberOfRegions
expr_stmt|;
block|}
comment|/**    * Originally, this method factored in the effect of requests going to the    * server as well. However, this does not interact very well with the current    * region rebalancing code, which only factors number of regions. For the     * interim, until we can figure out how to make rebalancing use all the info    * available, we're just going to make load purely the number of regions.    *    * @return load factor for this server    */
specifier|public
name|int
name|getLoad
parameter_list|()
block|{
comment|// int load = numberOfRequests == 0 ? 1 : numberOfRequests;
comment|// load *= numberOfRegions == 0 ? 1 : numberOfRegions;
comment|// return load;
return|return
name|numberOfRegions
return|;
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|toString
argument_list|(
literal|1
argument_list|)
return|;
block|}
comment|/**    * Returns toString() with the number of requests divided by the message interval in seconds    * @param msgInterval    * @return The load as a String    */
specifier|public
name|String
name|toString
parameter_list|(
name|int
name|msgInterval
parameter_list|)
block|{
return|return
literal|"requests: "
operator|+
name|numberOfRequests
operator|/
name|msgInterval
operator|+
literal|" regions: "
operator|+
name|numberOfRegions
return|;
block|}
comment|/** {@inheritDoc} */
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
return|return
name|compareTo
argument_list|(
name|o
argument_list|)
operator|==
literal|0
return|;
block|}
comment|/** {@inheritDoc} */
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
name|Integer
operator|.
name|valueOf
argument_list|(
name|numberOfRequests
argument_list|)
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|result
operator|^=
name|Integer
operator|.
name|valueOf
argument_list|(
name|numberOfRegions
argument_list|)
operator|.
name|hashCode
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
comment|// Getters
comment|/**    * @return the numberOfRegions    */
specifier|public
name|int
name|getNumberOfRegions
parameter_list|()
block|{
return|return
name|numberOfRegions
return|;
block|}
comment|/**    * @return the numberOfRequests    */
specifier|public
name|int
name|getNumberOfRequests
parameter_list|()
block|{
return|return
name|numberOfRequests
return|;
block|}
comment|// Setters
comment|/**    * @param numberOfRegions the numberOfRegions to set    */
specifier|public
name|void
name|setNumberOfRegions
parameter_list|(
name|int
name|numberOfRegions
parameter_list|)
block|{
name|this
operator|.
name|numberOfRegions
operator|=
name|numberOfRegions
expr_stmt|;
block|}
comment|/**    * @param numberOfRequests the numberOfRequests to set    */
specifier|public
name|void
name|setNumberOfRequests
parameter_list|(
name|int
name|numberOfRequests
parameter_list|)
block|{
name|this
operator|.
name|numberOfRequests
operator|=
name|numberOfRequests
expr_stmt|;
block|}
comment|// Writable
comment|/** {@inheritDoc} */
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|numberOfRequests
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|numberOfRegions
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
block|}
comment|/** {@inheritDoc} */
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|numberOfRequests
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|numberOfRegions
argument_list|)
expr_stmt|;
block|}
comment|// Comparable
comment|/** {@inheritDoc} */
specifier|public
name|int
name|compareTo
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
name|HServerLoad
name|other
init|=
operator|(
name|HServerLoad
operator|)
name|o
decl_stmt|;
return|return
name|this
operator|.
name|getLoad
argument_list|()
operator|-
name|other
operator|.
name|getLoad
argument_list|()
return|;
block|}
block|}
end_class

end_unit

