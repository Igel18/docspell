{-
   Copyright 2020 Eike K. & Contributors

   SPDX-License-Identifier: AGPL-3.0-or-later
-}


module Data.Flags exposing
    ( Config
    , Flags
    , accountString
    , getToken
    , withAccount
    , withoutAccount
    )

import Api.Model.AuthResult exposing (AuthResult)


type alias OpenIdAuth =
    { provider : String
    , name : String
    }


type alias Config =
    { appName : String
    , baseUrl : String
    , signupMode : String
    , docspellAssetPath : String
    , integrationEnabled : Bool
    , fullTextSearchEnabled : Bool
    , maxPageSize : Int
    , maxNoteLength : Int
    , showClassificationSettings : Bool
    , openIdAuth : List OpenIdAuth
    }


type alias Flags =
    { account : Maybe AuthResult
    , config : Config
    }


getToken : Flags -> Maybe String
getToken flags =
    flags.account
        |> Maybe.andThen (\a -> a.token)


withAccount : Flags -> AuthResult -> Flags
withAccount flags acc =
    { flags | account = Just acc }


withoutAccount : Flags -> Flags
withoutAccount flags =
    { flags | account = Nothing }


accountString : AuthResult -> String
accountString auth =
    if auth.collective == auth.user then
        auth.collective

    else
        auth.collective ++ "/" ++ auth.user
