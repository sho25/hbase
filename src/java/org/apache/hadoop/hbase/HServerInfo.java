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
comment|/**  * HServerInfo contains metainfo about an HRegionServer, Currently it only  * contains the server start code.  *   * In the future it will contain information about the source machine and  * load statistics.  */
end_comment

begin_class
specifier|public
class|class
name|HServerInfo
implements|implements
name|WritableComparable
argument_list|<
name|HServerInfo
argument_list|>
block|{
specifier|private
name|HServerAddress
name|serverAddress
decl_stmt|;
specifier|private
name|long
name|startCode
decl_stmt|;
specifier|private
name|HServerLoad
name|load
decl_stmt|;
specifier|private
name|int
name|infoPort
decl_stmt|;
specifier|private
name|String
name|serverName
init|=
literal|null
decl_stmt|;
specifier|private
name|String
name|name
decl_stmt|;
comment|/** default constructor - used by Writable */
specifier|public
name|HServerInfo
parameter_list|()
block|{
name|this
argument_list|(
operator|new
name|HServerAddress
argument_list|()
argument_list|,
literal|0
argument_list|,
name|HConstants
operator|.
name|DEFAULT_REGIONSERVER_INFOPORT
argument_list|,
literal|"default name"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor    * @param serverAddress    * @param startCode    * @param infoPort Port the info server is listening on.    */
specifier|public
name|HServerInfo
parameter_list|(
name|HServerAddress
name|serverAddress
parameter_list|,
name|long
name|startCode
parameter_list|,
specifier|final
name|int
name|infoPort
parameter_list|,
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|serverAddress
operator|=
name|serverAddress
expr_stmt|;
name|this
operator|.
name|startCode
operator|=
name|startCode
expr_stmt|;
name|this
operator|.
name|load
operator|=
operator|new
name|HServerLoad
argument_list|()
expr_stmt|;
name|this
operator|.
name|infoPort
operator|=
name|infoPort
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
comment|/**    * Construct a new object using another as input (like a copy constructor)    * @param other    */
specifier|public
name|HServerInfo
parameter_list|(
name|HServerInfo
name|other
parameter_list|)
block|{
name|this
operator|.
name|serverAddress
operator|=
operator|new
name|HServerAddress
argument_list|(
name|other
operator|.
name|getServerAddress
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|startCode
operator|=
name|other
operator|.
name|getStartCode
argument_list|()
expr_stmt|;
name|this
operator|.
name|load
operator|=
name|other
operator|.
name|getLoad
argument_list|()
expr_stmt|;
name|this
operator|.
name|infoPort
operator|=
name|other
operator|.
name|getInfoPort
argument_list|()
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|other
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
comment|/**    * @return the load    */
specifier|public
name|HServerLoad
name|getLoad
parameter_list|()
block|{
return|return
name|load
return|;
block|}
comment|/**    * @param load the load to set    */
specifier|public
name|void
name|setLoad
parameter_list|(
name|HServerLoad
name|load
parameter_list|)
block|{
name|this
operator|.
name|load
operator|=
name|load
expr_stmt|;
block|}
comment|/** @return the server address */
specifier|public
specifier|synchronized
name|HServerAddress
name|getServerAddress
parameter_list|()
block|{
return|return
operator|new
name|HServerAddress
argument_list|(
name|serverAddress
argument_list|)
return|;
block|}
comment|/**    * Change the server address.    * @param serverAddress New server address    */
specifier|public
specifier|synchronized
name|void
name|setServerAddress
parameter_list|(
name|HServerAddress
name|serverAddress
parameter_list|)
block|{
name|this
operator|.
name|serverAddress
operator|=
name|serverAddress
expr_stmt|;
name|this
operator|.
name|serverName
operator|=
literal|null
expr_stmt|;
block|}
comment|/** @return the server start code */
specifier|public
specifier|synchronized
name|long
name|getStartCode
parameter_list|()
block|{
return|return
name|startCode
return|;
block|}
comment|/**    * @return Port the info server is listening on.    */
specifier|public
name|int
name|getInfoPort
parameter_list|()
block|{
return|return
name|this
operator|.
name|infoPort
return|;
block|}
comment|/**    * @param infoPort - new port of info server    */
specifier|public
name|void
name|setInfoPort
parameter_list|(
name|int
name|infoPort
parameter_list|)
block|{
name|this
operator|.
name|infoPort
operator|=
name|infoPort
expr_stmt|;
block|}
comment|/**    * @param startCode the startCode to set    */
specifier|public
specifier|synchronized
name|void
name|setStartCode
parameter_list|(
name|long
name|startCode
parameter_list|)
block|{
name|this
operator|.
name|startCode
operator|=
name|startCode
expr_stmt|;
name|this
operator|.
name|serverName
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * @return the server name in the form hostname_startcode_port    */
specifier|public
specifier|synchronized
name|String
name|getServerName
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|serverName
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|serverName
operator|=
name|getServerName
argument_list|(
name|this
operator|.
name|serverAddress
argument_list|,
name|this
operator|.
name|startCode
argument_list|)
expr_stmt|;
block|}
return|return
name|this
operator|.
name|serverName
return|;
block|}
comment|/**    * Get the hostname of the server    * @return hostname    */
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
comment|/**    * Set the hostname of the server    * @param name hostname    */
specifier|public
name|void
name|setName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
comment|/**    * @see java.lang.Object#toString()    */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"address: "
operator|+
name|this
operator|.
name|serverAddress
operator|+
literal|", startcode: "
operator|+
name|this
operator|.
name|startCode
operator|+
literal|", load: ("
operator|+
name|this
operator|.
name|load
operator|.
name|toString
argument_list|()
operator|+
literal|")"
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
return|return
name|compareTo
argument_list|(
operator|(
name|HServerInfo
operator|)
name|obj
argument_list|)
operator|==
literal|0
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
return|return
name|this
operator|.
name|getServerName
argument_list|()
operator|.
name|hashCode
argument_list|()
return|;
block|}
comment|// Writable
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
name|this
operator|.
name|serverAddress
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|startCode
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|this
operator|.
name|load
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|infoPort
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
block|}
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
name|this
operator|.
name|serverAddress
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|this
operator|.
name|startCode
argument_list|)
expr_stmt|;
name|this
operator|.
name|load
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|this
operator|.
name|infoPort
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|compareTo
parameter_list|(
name|HServerInfo
name|o
parameter_list|)
block|{
return|return
name|this
operator|.
name|getServerName
argument_list|()
operator|.
name|compareTo
argument_list|(
name|o
operator|.
name|getServerName
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @param info    * @return the server name in the form hostname_startcode_port    */
specifier|public
specifier|static
name|String
name|getServerName
parameter_list|(
name|HServerInfo
name|info
parameter_list|)
block|{
return|return
name|getServerName
argument_list|(
name|info
operator|.
name|getServerAddress
argument_list|()
argument_list|,
name|info
operator|.
name|getStartCode
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @param serverAddress in the form hostname:port    * @param startCode    * @return the server name in the form hostname_startcode_port    */
specifier|public
specifier|static
name|String
name|getServerName
parameter_list|(
name|String
name|serverAddress
parameter_list|,
name|long
name|startCode
parameter_list|)
block|{
name|String
name|name
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|serverAddress
operator|!=
literal|null
condition|)
block|{
name|int
name|colonIndex
init|=
name|serverAddress
operator|.
name|lastIndexOf
argument_list|(
literal|':'
argument_list|)
decl_stmt|;
if|if
condition|(
name|colonIndex
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Not a host:port pair: "
operator|+
name|serverAddress
argument_list|)
throw|;
block|}
name|String
name|host
init|=
name|serverAddress
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|colonIndex
argument_list|)
decl_stmt|;
name|int
name|port
init|=
name|Integer
operator|.
name|valueOf
argument_list|(
name|serverAddress
operator|.
name|substring
argument_list|(
name|colonIndex
operator|+
literal|1
argument_list|)
argument_list|)
operator|.
name|intValue
argument_list|()
decl_stmt|;
name|name
operator|=
name|getServerName
argument_list|(
name|host
argument_list|,
name|port
argument_list|,
name|startCode
argument_list|)
expr_stmt|;
name|HServerAddress
name|address
init|=
operator|new
name|HServerAddress
argument_list|(
name|serverAddress
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|address
operator|.
name|getHostname
argument_list|()
operator|.
name|equals
argument_list|(
name|host
argument_list|)
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"HBASE-1918 debug : "
operator|+
name|address
operator|.
name|getHostname
argument_list|()
operator|+
literal|" != "
operator|+
name|host
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|name
return|;
block|}
comment|/**    * @param address    * @param startCode    * @return the server name in the form hostname_startcode_port    */
specifier|public
specifier|static
name|String
name|getServerName
parameter_list|(
name|HServerAddress
name|address
parameter_list|,
name|long
name|startCode
parameter_list|)
block|{
return|return
name|getServerName
argument_list|(
name|address
operator|.
name|getHostname
argument_list|()
argument_list|,
name|address
operator|.
name|getPort
argument_list|()
argument_list|,
name|startCode
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|String
name|getServerName
parameter_list|(
name|String
name|hostName
parameter_list|,
name|int
name|port
parameter_list|,
name|long
name|startCode
parameter_list|)
block|{
name|StringBuilder
name|name
init|=
operator|new
name|StringBuilder
argument_list|(
name|hostName
argument_list|)
decl_stmt|;
name|name
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
name|name
operator|.
name|append
argument_list|(
name|port
argument_list|)
expr_stmt|;
name|name
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
name|name
operator|.
name|append
argument_list|(
name|startCode
argument_list|)
expr_stmt|;
return|return
name|name
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

