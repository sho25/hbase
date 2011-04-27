begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|java
operator|.
name|net
operator|.
name|InetSocketAddress
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
name|HRegionServer
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
name|VersionedWritable
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
comment|/**  * HServerInfo is meta info about an {@link HRegionServer}.  It hosts the  * {@link HServerAddress}, its webui port, and its server startcode.  It was  * used to pass meta info about a server across an RPC but we've since made  * it so regionserver info is up in ZooKeeper and so this class is on its  * way out. It used to carry {@link HServerLoad} but as off HBase 0.92.0, the  * HServerLoad is passed independent of this class. Also, we now no longer pass  * the webui from regionserver to master (TODO: Fix).  * @deprecated Use {@link InetSocketAddress} and or {@link ServerName} and or  * {@link HServerLoad}  */
end_comment

begin_class
specifier|public
class|class
name|HServerInfo
extends|extends
name|VersionedWritable
implements|implements
name|WritableComparable
argument_list|<
name|HServerInfo
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|byte
name|VERSION
init|=
literal|1
decl_stmt|;
specifier|private
name|HServerAddress
name|serverAddress
init|=
operator|new
name|HServerAddress
argument_list|()
decl_stmt|;
specifier|private
name|long
name|startCode
decl_stmt|;
specifier|private
name|int
name|webuiport
decl_stmt|;
specifier|public
name|HServerInfo
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Constructor that creates a HServerInfo with a generated startcode    * @param serverAddress    * @param webuiport Port the webui runs on.    */
specifier|public
name|HServerInfo
parameter_list|(
specifier|final
name|HServerAddress
name|serverAddress
parameter_list|,
specifier|final
name|int
name|webuiport
parameter_list|)
block|{
name|this
argument_list|(
name|serverAddress
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|webuiport
argument_list|)
expr_stmt|;
block|}
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
name|webuiport
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
name|webuiport
operator|=
name|webuiport
expr_stmt|;
block|}
comment|/**    * Copy-constructor    * @param other    */
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
name|webuiport
operator|=
name|other
operator|.
name|getInfoPort
argument_list|()
expr_stmt|;
block|}
comment|/** @return the object version number */
specifier|public
name|byte
name|getVersion
parameter_list|()
block|{
return|return
name|VERSION
return|;
block|}
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
specifier|public
name|int
name|getInfoPort
parameter_list|()
block|{
return|return
name|getWebuiPort
argument_list|()
return|;
block|}
specifier|public
name|int
name|getWebuiPort
parameter_list|()
block|{
return|return
name|this
operator|.
name|webuiport
return|;
block|}
specifier|public
name|String
name|getHostname
parameter_list|()
block|{
return|return
name|this
operator|.
name|serverAddress
operator|.
name|getHostname
argument_list|()
return|;
block|}
comment|/**    * @return ServerName and load concatenated.    * @see #getServerName()    * @see #getLoad()    */
annotation|@
name|Override
specifier|public
specifier|synchronized
name|String
name|toString
parameter_list|()
block|{
return|return
name|ServerName
operator|.
name|getServerName
argument_list|(
name|this
operator|.
name|serverAddress
operator|.
name|getHostnameAndPort
argument_list|()
argument_list|,
name|this
operator|.
name|startCode
argument_list|)
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
return|return
literal|true
return|;
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
return|return
literal|false
return|;
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
return|return
literal|false
return|;
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
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|code
init|=
name|this
operator|.
name|serverAddress
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|code
operator|^=
name|this
operator|.
name|webuiport
expr_stmt|;
name|code
operator|^=
name|this
operator|.
name|startCode
expr_stmt|;
return|return
name|code
return|;
block|}
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
name|super
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
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
name|webuiport
operator|=
name|in
operator|.
name|readInt
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
name|super
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
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
name|out
operator|.
name|writeInt
argument_list|(
name|this
operator|.
name|webuiport
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
name|int
name|compare
init|=
name|this
operator|.
name|serverAddress
operator|.
name|compareTo
argument_list|(
name|o
operator|.
name|getServerAddress
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|compare
operator|!=
literal|0
condition|)
return|return
name|compare
return|;
if|if
condition|(
name|this
operator|.
name|webuiport
operator|!=
name|o
operator|.
name|getInfoPort
argument_list|()
condition|)
return|return
name|this
operator|.
name|webuiport
operator|-
name|o
operator|.
name|getInfoPort
argument_list|()
return|;
if|if
condition|(
name|this
operator|.
name|startCode
operator|!=
name|o
operator|.
name|getStartCode
argument_list|()
condition|)
return|return
call|(
name|int
call|)
argument_list|(
name|this
operator|.
name|startCode
operator|-
name|o
operator|.
name|getStartCode
argument_list|()
argument_list|)
return|;
return|return
literal|0
return|;
block|}
block|}
end_class

end_unit

