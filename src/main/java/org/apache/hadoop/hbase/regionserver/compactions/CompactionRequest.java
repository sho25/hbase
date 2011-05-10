begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|compactions
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Date
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
name|regionserver
operator|.
name|HRegion
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
name|regionserver
operator|.
name|StoreFile
import|;
end_import

begin_comment
comment|/**    * This class represents a compaction request and holds the region, priority,    * and time submitted.    */
end_comment

begin_class
specifier|public
class|class
name|CompactionRequest
implements|implements
name|Comparable
argument_list|<
name|CompactionRequest
argument_list|>
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|CompactionRequest
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HRegion
name|r
decl_stmt|;
specifier|private
specifier|final
name|Store
name|s
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|StoreFile
argument_list|>
name|files
decl_stmt|;
specifier|private
specifier|final
name|long
name|totalSize
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isMajor
decl_stmt|;
specifier|private
name|int
name|p
decl_stmt|;
specifier|private
specifier|final
name|Date
name|date
decl_stmt|;
specifier|public
name|CompactionRequest
parameter_list|(
name|HRegion
name|r
parameter_list|,
name|Store
name|s
parameter_list|)
block|{
name|this
argument_list|(
name|r
argument_list|,
name|s
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
name|s
operator|.
name|getCompactPriority
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|CompactionRequest
parameter_list|(
name|HRegion
name|r
parameter_list|,
name|Store
name|s
parameter_list|,
name|int
name|p
parameter_list|)
block|{
name|this
argument_list|(
name|r
argument_list|,
name|s
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
name|p
argument_list|)
expr_stmt|;
block|}
specifier|public
name|CompactionRequest
parameter_list|(
name|HRegion
name|r
parameter_list|,
name|Store
name|s
parameter_list|,
name|List
argument_list|<
name|StoreFile
argument_list|>
name|files
parameter_list|,
name|boolean
name|isMajor
parameter_list|,
name|int
name|p
parameter_list|)
block|{
if|if
condition|(
name|r
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"HRegion cannot be null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|r
operator|=
name|r
expr_stmt|;
name|this
operator|.
name|s
operator|=
name|s
expr_stmt|;
name|this
operator|.
name|files
operator|=
name|files
expr_stmt|;
name|long
name|sz
init|=
literal|0
decl_stmt|;
for|for
control|(
name|StoreFile
name|sf
range|:
name|files
control|)
block|{
name|sz
operator|+=
name|sf
operator|.
name|getReader
argument_list|()
operator|.
name|length
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|totalSize
operator|=
name|sz
expr_stmt|;
name|this
operator|.
name|isMajor
operator|=
name|isMajor
expr_stmt|;
name|this
operator|.
name|p
operator|=
name|p
expr_stmt|;
name|this
operator|.
name|date
operator|=
operator|new
name|Date
argument_list|()
expr_stmt|;
block|}
comment|/**      * This function will define where in the priority queue the request will      * end up.  Those with the highest priorities will be first.  When the      * priorities are the same it will It will first compare priority then date      * to maintain a FIFO functionality.      *      *<p>Note: The date is only accurate to the millisecond which means it is      * possible that two requests were inserted into the queue within a      * millisecond.  When that is the case this function will break the tie      * arbitrarily.      */
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|CompactionRequest
name|request
parameter_list|)
block|{
comment|//NOTE: The head of the priority queue is the least element
if|if
condition|(
name|this
operator|.
name|equals
argument_list|(
name|request
argument_list|)
condition|)
block|{
return|return
literal|0
return|;
comment|//they are the same request
block|}
name|int
name|compareVal
decl_stmt|;
name|compareVal
operator|=
name|p
operator|-
name|request
operator|.
name|p
expr_stmt|;
comment|//compare priority
if|if
condition|(
name|compareVal
operator|!=
literal|0
condition|)
block|{
return|return
name|compareVal
return|;
block|}
name|compareVal
operator|=
name|date
operator|.
name|compareTo
argument_list|(
name|request
operator|.
name|date
argument_list|)
expr_stmt|;
if|if
condition|(
name|compareVal
operator|!=
literal|0
condition|)
block|{
return|return
name|compareVal
return|;
block|}
comment|// break the tie based on hash code
return|return
name|this
operator|.
name|hashCode
argument_list|()
operator|-
name|request
operator|.
name|hashCode
argument_list|()
return|;
block|}
comment|/** Gets the HRegion for the request */
specifier|public
name|HRegion
name|getHRegion
parameter_list|()
block|{
return|return
name|r
return|;
block|}
comment|/** Gets the Store for the request */
specifier|public
name|Store
name|getStore
parameter_list|()
block|{
return|return
name|s
return|;
block|}
comment|/** Gets the StoreFiles for the request */
specifier|public
name|List
argument_list|<
name|StoreFile
argument_list|>
name|getFiles
parameter_list|()
block|{
return|return
name|files
return|;
block|}
comment|/** Gets the total size of all StoreFiles in compaction */
specifier|public
name|long
name|getSize
parameter_list|()
block|{
return|return
name|totalSize
return|;
block|}
specifier|public
name|boolean
name|isMajor
parameter_list|()
block|{
return|return
name|this
operator|.
name|isMajor
return|;
block|}
comment|/** Gets the priority for the request */
specifier|public
name|int
name|getPriority
parameter_list|()
block|{
return|return
name|p
return|;
block|}
comment|/** Gets the priority for the request */
specifier|public
name|void
name|setPriority
parameter_list|(
name|int
name|p
parameter_list|)
block|{
name|this
operator|.
name|p
operator|=
name|p
expr_stmt|;
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"regionName="
operator|+
name|r
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|", storeName="
operator|+
operator|new
name|String
argument_list|(
name|s
operator|.
name|getFamily
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
operator|+
literal|", fileCount="
operator|+
name|files
operator|.
name|size
argument_list|()
operator|+
literal|", priority="
operator|+
name|p
operator|+
literal|", date="
operator|+
name|date
return|;
block|}
block|}
end_class

end_unit

