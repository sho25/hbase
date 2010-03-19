begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|stargate
package|;
end_package

begin_comment
comment|/**  * Common constants for org.apache.hadoop.hbase.stargate  */
end_comment

begin_interface
specifier|public
interface|interface
name|Constants
block|{
specifier|public
specifier|static
specifier|final
name|String
name|VERSION_STRING
init|=
literal|"0.0.2"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|AUTHENTICATOR_KEY
init|=
literal|"stargate.authenticator"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MULTIUSER_KEY
init|=
literal|"stargate.multiuser"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|STATUS_REPORT_PERIOD_KEY
init|=
literal|"stargate.status.period"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|USERS_TABLE
init|=
literal|"users"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|INSTANCE_ZNODE_ROOT
init|=
literal|"/stargate/instance"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|USERS_ZNODE_ROOT
init|=
literal|"/stargate/users"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_MAX_AGE
init|=
literal|60
operator|*
literal|60
operator|*
literal|4
decl_stmt|;
comment|// 4 hours
specifier|public
specifier|static
specifier|final
name|String
name|MIMETYPE_TEXT
init|=
literal|"text/plain"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MIMETYPE_HTML
init|=
literal|"text/html"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MIMETYPE_XML
init|=
literal|"text/xml"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MIMETYPE_BINARY
init|=
literal|"application/octet-stream"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MIMETYPE_PROTOBUF
init|=
literal|"application/x-protobuf"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MIMETYPE_JSON
init|=
literal|"application/json"
decl_stmt|;
block|}
end_interface

end_unit

