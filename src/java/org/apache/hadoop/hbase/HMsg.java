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
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|Writable
import|;
end_import

begin_comment
comment|/**  * HMsg is for communicating instructions between the HMaster and the   * HRegionServers.  *   * Most of the time the messages are simple but some messages are accompanied  * by the region affected.  HMsg may also carry optional message.  */
end_comment

begin_class
specifier|public
class|class
name|HMsg
implements|implements
name|Writable
block|{
comment|/**    * Message types sent between master and regionservers    */
specifier|public
specifier|static
enum|enum
name|Type
block|{
comment|/** null message */
name|MSG_NONE
block|,
comment|// Message types sent from master to region server
comment|/** Start serving the specified region */
name|MSG_REGION_OPEN
block|,
comment|/** Stop serving the specified region */
name|MSG_REGION_CLOSE
block|,
comment|/** Split the specified region */
name|MSG_REGION_SPLIT
block|,
comment|/** Compact the specified region */
name|MSG_REGION_COMPACT
block|,
comment|/** Region server is unknown to master. Restart */
name|MSG_CALL_SERVER_STARTUP
block|,
comment|/** Master tells region server to stop */
name|MSG_REGIONSERVER_STOP
block|,
comment|/** Stop serving the specified region and don't report back that it's      * closed      */
name|MSG_REGION_CLOSE_WITHOUT_REPORT
block|,
comment|/** Stop serving user regions */
name|MSG_REGIONSERVER_QUIESCE
block|,
comment|// Message types sent from the region server to the master
comment|/** region server is now serving the specified region */
name|MSG_REPORT_OPEN
block|,
comment|/** region server is no longer serving the specified region */
name|MSG_REPORT_CLOSE
block|,
comment|/** region server is processing open request */
name|MSG_REPORT_PROCESS_OPEN
block|,
comment|/**      * Region server split the region associated with this message.      *       * Note that this message is immediately followed by two MSG_REPORT_OPEN      * messages, one for each of the new regions resulting from the split      */
name|MSG_REPORT_SPLIT
block|,
comment|/**      * Region server is shutting down      *       * Note that this message is followed by MSG_REPORT_CLOSE messages for each      * region the region server was serving, unless it was told to quiesce.      */
name|MSG_REPORT_EXITING
block|,
comment|/** Region server has closed all user regions but is still serving meta      * regions      */
name|MSG_REPORT_QUIESCED
block|,
comment|/**      * Flush      */
name|MSG_REGION_FLUSH
block|,
comment|/**      * Run Major Compaction      */
name|MSG_REGION_MAJOR_COMPACT
block|,   }
specifier|private
name|Type
name|type
init|=
literal|null
decl_stmt|;
specifier|private
name|HRegionInfo
name|info
init|=
literal|null
decl_stmt|;
specifier|private
name|byte
index|[]
name|message
init|=
literal|null
decl_stmt|;
comment|/** Default constructor. Used during deserialization */
specifier|public
name|HMsg
parameter_list|()
block|{
name|this
argument_list|(
name|Type
operator|.
name|MSG_NONE
argument_list|)
expr_stmt|;
block|}
comment|/**    * Construct a message with the specified message and empty HRegionInfo    * @param type Message type    */
specifier|public
name|HMsg
parameter_list|(
specifier|final
name|HMsg
operator|.
name|Type
name|type
parameter_list|)
block|{
name|this
argument_list|(
name|type
argument_list|,
operator|new
name|HRegionInfo
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Construct a message with the specified message and HRegionInfo    * @param type Message type    * @param hri Region to which message<code>type</code> applies    */
specifier|public
name|HMsg
parameter_list|(
specifier|final
name|HMsg
operator|.
name|Type
name|type
parameter_list|,
specifier|final
name|HRegionInfo
name|hri
parameter_list|)
block|{
name|this
argument_list|(
name|type
argument_list|,
name|hri
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Construct a message with the specified message and HRegionInfo    *     * @param type Message type    * @param hri Region to which message<code>type</code> applies.  Cannot be    * null.  If no info associated, used other Constructor.    * @param msg Optional message (Stringified exception, etc.)    */
specifier|public
name|HMsg
parameter_list|(
specifier|final
name|HMsg
operator|.
name|Type
name|type
parameter_list|,
specifier|final
name|HRegionInfo
name|hri
parameter_list|,
specifier|final
name|byte
index|[]
name|msg
parameter_list|)
block|{
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"Message type cannot be null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
if|if
condition|(
name|hri
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"Region cannot be null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|info
operator|=
name|hri
expr_stmt|;
name|this
operator|.
name|message
operator|=
name|msg
expr_stmt|;
block|}
comment|/**    * @return Region info or null if none associated with this message type.    */
specifier|public
name|HRegionInfo
name|getRegionInfo
parameter_list|()
block|{
return|return
name|this
operator|.
name|info
return|;
block|}
comment|/** @return the type of message */
specifier|public
name|Type
name|getType
parameter_list|()
block|{
return|return
name|this
operator|.
name|type
return|;
block|}
comment|/**    * @param other Message type to compare to    * @return True if we are of same message type as<code>other</code>    */
specifier|public
name|boolean
name|isType
parameter_list|(
specifier|final
name|HMsg
operator|.
name|Type
name|other
parameter_list|)
block|{
return|return
name|this
operator|.
name|type
operator|.
name|equals
argument_list|(
name|other
argument_list|)
return|;
block|}
comment|/** @return the message type */
specifier|public
name|byte
index|[]
name|getMessage
parameter_list|()
block|{
return|return
name|this
operator|.
name|message
return|;
block|}
comment|/**    * @see java.lang.Object#toString()    */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|type
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// If null or empty region, don't bother printing it out.
if|if
condition|(
name|this
operator|.
name|info
operator|!=
literal|null
operator|&&
name|this
operator|.
name|info
operator|.
name|getRegionName
argument_list|()
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|": "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|info
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|message
operator|!=
literal|null
operator|&&
name|this
operator|.
name|message
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|": "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|this
operator|.
name|message
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * @see java.lang.Object#equals(java.lang.Object)    */
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
name|HMsg
name|that
init|=
operator|(
name|HMsg
operator|)
name|obj
decl_stmt|;
return|return
name|this
operator|.
name|type
operator|.
name|equals
argument_list|(
name|that
operator|.
name|type
argument_list|)
operator|&&
operator|(
name|this
operator|.
name|info
operator|!=
literal|null
operator|)
condition|?
name|this
operator|.
name|info
operator|.
name|equals
argument_list|(
name|that
operator|.
name|info
argument_list|)
else|:
name|that
operator|.
name|info
operator|==
literal|null
return|;
block|}
comment|/**    * @see java.lang.Object#hashCode()    */
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
name|this
operator|.
name|type
operator|.
name|hashCode
argument_list|()
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|info
operator|!=
literal|null
condition|)
block|{
name|result
operator|^=
name|this
operator|.
name|info
operator|.
name|hashCode
argument_list|()
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|// ////////////////////////////////////////////////////////////////////////////
comment|// Writable
comment|//////////////////////////////////////////////////////////////////////////////
comment|/**    * @see org.apache.hadoop.io.Writable#write(java.io.DataOutput)    */
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
name|this
operator|.
name|type
operator|.
name|ordinal
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|info
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|message
operator|==
literal|null
operator|||
name|this
operator|.
name|message
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|this
operator|.
name|message
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @see org.apache.hadoop.io.Writable#readFields(java.io.DataInput)    */
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
name|int
name|ordinal
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|this
operator|.
name|type
operator|=
name|HMsg
operator|.
name|Type
operator|.
name|values
argument_list|()
index|[
name|ordinal
index|]
expr_stmt|;
name|this
operator|.
name|info
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|boolean
name|hasMessage
init|=
name|in
operator|.
name|readBoolean
argument_list|()
decl_stmt|;
if|if
condition|(
name|hasMessage
condition|)
block|{
name|this
operator|.
name|message
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

